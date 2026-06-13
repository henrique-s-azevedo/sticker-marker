package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.*;
import com.henrique.stickermarker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates sticker sell proposals between friends.
 *
 * <p>The sell flow supports two directions:
 * <ul>
 *   <li>{@link #proposeSell} — seller initiates: "I want to sell you these stickers."</li>
 *   <li>{@link #proposeBuy} — buyer initiates: "I want to buy these stickers from you."</li>
 * </ul>
 * Both paths create the same {@link SellProposal} model (seller=giver, buyer=receiver),
 * but generate different system message types (SELL_PROPOSAL vs BUY_PROPOSAL) so the UI
 * can display direction-appropriate wording.</p>
 *
 * <p>Sell lifecycle: {@code PENDING} → {@code COMPLETED} or {@code CANCELLED}.
 * There is no multi-step negotiation — either party can directly complete or cancel.</p>
 *
 * <p>Stickers are grouped into price batches ({@link SellBatchDTO}) where each batch
 * has a per-unit price. The {@code batchIndex} field on {@link SellProposalItem} preserves
 * grouping when the proposal is reconstructed from the database.</p>
 *
 * <p>{@code COLLECTION_ID = 1L} is hardcoded because the system currently supports one album.</p>
 */
@Service
@RequiredArgsConstructor
public class SellService {

    private static final Long COLLECTION_ID = 1L;

    private final SellProposalRepository sellProposalRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CollectionService collectionService;
    private final MessageService messageService;
    private final StickerService stickerService;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

    /**
     * Calculates which of the seller's duplicate stickers the buyer is missing.
     * Used to pre-populate the sell proposal form from the seller's perspective.
     *
     * @param sellerId the potential seller's ID
     * @param buyerId  the potential buyer's ID
     * @return the available stickers the seller has that the buyer is missing
     * @throws IllegalArgumentException if the users are not friends
     */
    public SellCalculationDTO calculateSell(Long sellerId, Long buyerId) {
        if (!friendshipService.areFriends(sellerId, buyerId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        return buildCalculation(sellerId, buyerId);
    }

    /**
     * Calculates which of the seller's duplicate stickers the buyer is missing.
     * Same as {@link #calculateSell} but called from the buyer's perspective.
     *
     * @param buyerId  the buyer initiating the calculation
     * @param sellerId the potential seller's ID
     * @return the available stickers the seller has that the buyer is missing
     * @throws IllegalArgumentException if the users are not friends
     */
    public SellCalculationDTO calculateBuy(Long buyerId, Long sellerId) {
        if (!friendshipService.areFriends(buyerId, sellerId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        return buildCalculation(sellerId, buyerId);
    }

    /**
     * Creates a sell proposal initiated by the seller.
     * Side effect: sends a SELL_PROPOSAL system message to the buyer's conversation.
     *
     * @param sellerId the seller's ID
     * @param buyerId  the buyer's ID
     * @param dto      the batches of stickers and prices
     * @return the created proposal with {@code PENDING} status
     * @throws IllegalArgumentException if users are not friends or batches are invalid
     */
    @Transactional
    public SellProposalDTO proposeSell(Long sellerId, Long buyerId, CreateSellDTO dto) {
        if (!friendshipService.areFriends(sellerId, buyerId)) {
            throw new IllegalArgumentException("Só podes propor vendas a amigos");
        }
        validateBatches(dto.getBatches());

        User seller = userService.getById(sellerId);
        User buyer = userService.getById(buyerId);

        SellProposal proposal = buildProposal(seller, buyer, dto.getBatches());
        SellProposal saved = sellProposalRepository.save(proposal);

        String content = buildMessageContent(buyer.getDisplayName(), dto.getBatches(), true);
        messageService.sendSellInternal(sellerId, buyerId, content, MessageType.SELL_PROPOSAL, saved.getId());

        return toDTO(saved);
    }

    /**
     * Creates a sell proposal initiated by the buyer.
     * Roles are reversed: the buyer calls this but the resulting proposal has the
     * friend as seller and the caller as buyer.
     * Side effect: sends a BUY_PROPOSAL system message to the seller's conversation.
     *
     * @param buyerId  the buyer's ID (the initiator of this call)
     * @param sellerId the seller's ID
     * @param dto      the batches of stickers and prices
     * @return the created proposal with {@code PENDING} status
     * @throws IllegalArgumentException if users are not friends or batches are invalid
     */
    @Transactional
    public SellProposalDTO proposeBuy(Long buyerId, Long sellerId, CreateSellDTO dto) {
        if (!friendshipService.areFriends(buyerId, sellerId)) {
            throw new IllegalArgumentException("Só podes propor compras a amigos");
        }
        validateBatches(dto.getBatches());

        User buyer = userService.getById(buyerId);
        User seller = userService.getById(sellerId);

        SellProposal proposal = buildProposal(seller, buyer, dto.getBatches());
        SellProposal saved = sellProposalRepository.save(proposal);

        String content = buildMessageContent(seller.getDisplayName(), dto.getBatches(), false);
        messageService.sendSellInternal(buyerId, sellerId, content, MessageType.BUY_PROPOSAL, saved.getId());

        return toDTO(saved);
    }

    /**
     * Marks a pending sell proposal as completed and transfers sticker inventory.
     * Either participant (seller or buyer) may complete it once the physical transaction occurs.
     *
     * @param sellId the proposal to complete
     * @param userId the user marking it done (must be seller or buyer)
     * @return the updated proposal with {@code COMPLETED} status
     * @throws IllegalArgumentException if the caller is not a participant or proposal is not PENDING
     */
    @Transactional
    public SellProposalDTO completeSell(Long sellId, Long userId) {
        SellProposal proposal = getAndValidate(sellId);
        boolean isParticipant = proposal.getSeller().getId().equals(userId)
                || proposal.getBuyer().getId().equals(userId);
        if (!isParticipant) {
            throw new IllegalArgumentException("Sem permissão para confirmar esta proposta");
        }
        if (proposal.getStatus() != SellProposalStatus.PENDING) {
            throw new IllegalArgumentException("Proposta já foi processada");
        }

        User seller = userService.getById(proposal.getSeller().getId());
        User buyer = userService.getById(proposal.getBuyer().getId());

        for (SellProposalItem item : proposal.getItems()) {
            transferDuplicate(seller, buyer, item.getStickerCode());
        }

        proposal.setStatus(SellProposalStatus.COMPLETED);
        return toDTO(sellProposalRepository.save(proposal));
    }

    /**
     * Cancels a pending sell proposal without any inventory changes.
     * Either participant may cancel before completion.
     *
     * @param sellId the proposal to cancel
     * @param userId the user cancelling (must be seller or buyer)
     * @return the updated proposal with {@code CANCELLED} status
     * @throws IllegalArgumentException if the caller is not a participant or proposal is not PENDING
     */
    @Transactional
    public SellProposalDTO cancelSell(Long sellId, Long userId) {
        SellProposal proposal = getAndValidate(sellId);
        boolean isParticipant = proposal.getSeller().getId().equals(userId)
                || proposal.getBuyer().getId().equals(userId);
        if (!isParticipant) throw new IllegalArgumentException("Sem permissão");
        if (proposal.getStatus() != SellProposalStatus.PENDING) {
            throw new IllegalArgumentException("Proposta já foi processada");
        }
        proposal.setStatus(SellProposalStatus.CANCELLED);
        return toDTO(sellProposalRepository.save(proposal));
    }

    /**
     * Returns a specific sell proposal. Only accessible to the seller or buyer.
     *
     * @param sellId the proposal ID
     * @param userId the requesting user (must be a participant)
     * @return the sell proposal DTO
     * @throws IllegalArgumentException if the user is not a participant
     */
    public SellProposalDTO getSell(Long sellId, Long userId) {
        SellProposal proposal = getAndValidate(sellId);
        boolean isParticipant = proposal.getSeller().getId().equals(userId)
                || proposal.getBuyer().getId().equals(userId);
        if (!isParticipant) throw new IllegalArgumentException("Sem permissão");
        return toDTO(proposal);
    }

    /**
     * Computes the intersection of seller's duplicates and buyer's missing stickers.
     * Always uses {@code COLLECTION_ID = 1L} as the current system only supports one album.
     */
    private SellCalculationDTO buildCalculation(Long sellerId, Long buyerId) {
        User seller = userService.getById(sellerId);
        User buyer = userService.getById(buyerId);

        List<CollectionStickerStatusDTO> sellerStickers = collectionService.getStickersWithStatus(seller, COLLECTION_ID);
        List<CollectionStickerStatusDTO> buyerStickers = collectionService.getStickersWithStatus(buyer, COLLECTION_ID);

        Map<String, CollectionStickerStatusDTO> sellerMap = sellerStickers.stream()
                .collect(Collectors.toMap(CollectionStickerStatusDTO::getCode, s -> s));

        Set<String> sellerDups = sellerStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.DUPLICATE)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());
        Set<String> buyerMissing = buyerStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.MISSING)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());

        List<StickerBriefDTO> available = sellerDups.stream()
                .filter(buyerMissing::contains)
                .sorted()
                .map(code -> toBriefDTO(sellerMap.get(code)))
                .collect(Collectors.toList());

        SellCalculationDTO dto = new SellCalculationDTO();
        dto.setFriendId(buyer.getId());
        dto.setFriendDisplayName(buyer.getDisplayName());
        dto.setFriendUserTag(buyer.getUserTag());
        dto.setAvailableStickers(available);
        return dto;
    }

    /**
     * Validates that the proposal has at least one non-empty batch with a non-negative price.
     *
     * @param batches the list of price batches to validate
     * @throws IllegalArgumentException on any validation failure
     */
    private void validateBatches(List<SellBatchDTO> batches) {
        if (batches == null || batches.isEmpty()) {
            throw new IllegalArgumentException("A proposta não tem cromos");
        }
        for (SellBatchDTO batch : batches) {
            if (batch.getStickerCodes() == null || batch.getStickerCodes().isEmpty()) {
                throw new IllegalArgumentException("Um dos grupos está vazio");
            }
            if (batch.getPricePerUnit() == null || batch.getPricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Preço inválido");
            }
        }
    }

    /**
     * Constructs the {@link SellProposal} entity from batches, assigning a {@code batchIndex}
     * to each item so the original price groupings can be reconstructed when reading back.
     */
    private SellProposal buildProposal(User seller, User buyer, List<SellBatchDTO> batches) {
        SellProposal proposal = new SellProposal();
        proposal.setSeller(seller);
        proposal.setBuyer(buyer);

        List<SellProposalItem> items = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            SellBatchDTO batch = batches.get(i);
            for (String code : batch.getStickerCodes()) {
                SellProposalItem item = new SellProposalItem();
                item.setSellProposal(proposal);
                item.setStickerCode(code);
                item.setPricePerUnit(batch.getPricePerUnit());
                item.setBatchIndex(i);
                items.add(item);
            }
        }
        proposal.setItems(items);
        return proposal;
    }

    /**
     * Builds the system message content for a sell or buy proposal notification.
     * Lists each sticker with its price and appends the total.
     *
     * @param recipientName the name of the recipient (buyer or seller depending on direction)
     * @param batches       the price batches from the proposal
     * @param isSelling     {@code true} if the initiator is the seller; {@code false} if the buyer
     * @return the formatted message string
     */
    private String buildMessageContent(String recipientName, List<SellBatchDTO> batches, boolean isSelling) {
        StringBuilder sb = new StringBuilder();
        sb.append(recipientName).append(isSelling ? ", quero vender:\n" : ", quero comprar:\n");
        BigDecimal total = BigDecimal.ZERO;
        for (SellBatchDTO batch : batches) {
            for (String code : batch.getStickerCodes()) {
                sb.append(code)
                  .append(" - ")
                  .append(String.format(Locale.ENGLISH, "%.2f€", batch.getPricePerUnit()))
                  .append("\n");
                total = total.add(batch.getPricePerUnit());
            }
        }
        sb.append("Total: ").append(String.format(Locale.ENGLISH, "%.2f€", total)).append("\n");
        sb.append("Envia mensagem a confirmar ou rejeitar.");
        return sb.toString();
    }

    /**
     * Transfers one unit of a sticker from the seller's duplicates to the buyer's inventory.
     * Logic mirrors {@link TradeService#transferDuplicate}: decrement or delete giver's duplicate,
     * then create owned or increment duplicate on the receiver side.
     *
     * @param giver    the seller (must have the sticker as a duplicate)
     * @param receiver the buyer
     * @param code     the sticker code to transfer
     * @throws IllegalStateException if the giver does not have the sticker as a duplicate
     */
    private void transferDuplicate(User giver, User receiver, String code) {
        var giverDup = userDuplicateRepository.findByUserAndSticker_Code(giver, code)
                .orElseThrow(() -> new IllegalStateException(
                        giver.getDisplayName() + " não tem " + code + " duplicado"));

        if (giverDup.getQuantity() > 1) {
            giverDup.setQuantity(giverDup.getQuantity() - 1);
            userDuplicateRepository.save(giverDup);
        } else {
            userDuplicateRepository.delete(giverDup);
        }

        var receiverOwns = userStickerRepository.findByUserAndSticker_Code(receiver, code);
        if (receiverOwns.isEmpty()) {
            Sticker sticker = stickerService.getByCode(code);
            UserSticker us = new UserSticker();
            us.setUser(receiver);
            us.setSticker(sticker);
            userStickerRepository.save(us);
        } else {
            var existingDup = userDuplicateRepository.findByUserAndSticker_Code(receiver, code);
            if (existingDup.isPresent()) {
                existingDup.get().setQuantity(existingDup.get().getQuantity() + 1);
                userDuplicateRepository.save(existingDup.get());
            } else {
                Sticker sticker = stickerService.getByCode(code);
                UserDuplicate dup = new UserDuplicate();
                dup.setUser(receiver);
                dup.setSticker(sticker);
                dup.setQuantity(1);
                userDuplicateRepository.save(dup);
            }
        }
    }

    private SellProposal getAndValidate(Long sellId) {
        return sellProposalRepository.findById(sellId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));
    }

    /**
     * Reconstructs price batches from the flat item list by grouping on {@code batchIndex},
     * then sorts by index to restore the original batch order.
     */
    private SellProposalDTO toDTO(SellProposal p) {
        SellProposalDTO dto = new SellProposalDTO();
        dto.setId(p.getId());
        dto.setSellerId(p.getSeller().getId());
        dto.setSellerName(p.getSeller().getDisplayName());
        dto.setSellerUserTag(p.getSeller().getUserTag());
        dto.setBuyerId(p.getBuyer().getId());
        dto.setBuyerName(p.getBuyer().getDisplayName());
        dto.setBuyerUserTag(p.getBuyer().getUserTag());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());

        Map<Integer, List<SellProposalItem>> byBatch = p.getItems().stream()
                .collect(Collectors.groupingBy(SellProposalItem::getBatchIndex));

        List<SellBatchDTO> batches = byBatch.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    SellBatchDTO b = new SellBatchDTO();
                    b.setStickerCodes(e.getValue().stream()
                            .map(SellProposalItem::getStickerCode)
                            .collect(Collectors.toList()));
                    b.setPricePerUnit(e.getValue().get(0).getPricePerUnit());
                    return b;
                })
                .collect(Collectors.toList());

        dto.setBatches(batches);
        dto.setTotal(p.getItems().stream()
                .map(SellProposalItem::getPricePerUnit)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return dto;
    }

    private StickerBriefDTO toBriefDTO(CollectionStickerStatusDTO s) {
        StickerBriefDTO dto = new StickerBriefDTO();
        dto.setCode(s.getCode());
        dto.setNumber(s.getNumber());
        dto.setPlayerName(s.getPlayerName());
        dto.setTeamName(s.getTeamName());
        dto.setTeamInitial(s.getTeamInitial());
        return dto;
    }
}
