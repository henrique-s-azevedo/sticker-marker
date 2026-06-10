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

    public SellCalculationDTO calculateSell(Long sellerId, Long buyerId) {
        if (!friendshipService.areFriends(sellerId, buyerId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        return buildCalculation(sellerId, buyerId);
    }

    public SellCalculationDTO calculateBuy(Long buyerId, Long sellerId) {
        if (!friendshipService.areFriends(buyerId, sellerId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        return buildCalculation(sellerId, buyerId);
    }

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

    public SellProposalDTO getSell(Long sellId, Long userId) {
        SellProposal proposal = getAndValidate(sellId);
        boolean isParticipant = proposal.getSeller().getId().equals(userId)
                || proposal.getBuyer().getId().equals(userId);
        if (!isParticipant) throw new IllegalArgumentException("Sem permissão");
        return toDTO(proposal);
    }

    // --- helpers ---

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
