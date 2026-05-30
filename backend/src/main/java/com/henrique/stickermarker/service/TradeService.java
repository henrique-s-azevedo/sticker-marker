package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.*;
import com.henrique.stickermarker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private static final Long COLLECTION_ID = 1L;

    private final TradeProposalRepository tradeProposalRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CollectionService collectionService;
    private final MessageService messageService;
    private final StickerService stickerService;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

    public TradeCalculationDTO calculate(Long myId, Long friendId) {
        if (!friendshipService.areFriends(myId, friendId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        User me = userService.getById(myId);
        User friend = userService.getById(friendId);

        List<CollectionStickerStatusDTO> myStickers = collectionService.getStickersWithStatus(me, COLLECTION_ID);
        List<CollectionStickerStatusDTO> friendStickers = collectionService.getStickersWithStatus(friend, COLLECTION_ID);

        Map<String, CollectionStickerStatusDTO> myStickerMap = myStickers.stream()
                .collect(Collectors.toMap(CollectionStickerStatusDTO::getCode, s -> s));
        Map<String, CollectionStickerStatusDTO> friendStickerMap = friendStickers.stream()
                .collect(Collectors.toMap(CollectionStickerStatusDTO::getCode, s -> s));

        Set<String> myDups = myStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.DUPLICATE)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());
        Set<String> myMissing = myStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.MISSING)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());
        Set<String> friendDups = friendStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.DUPLICATE)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());
        Set<String> friendMissing = friendStickers.stream()
                .filter(s -> s.getStatus() == StickerStatus.MISSING)
                .map(CollectionStickerStatusDTO::getCode).collect(Collectors.toSet());

        List<StickerBriefDTO> myOfferings = myDups.stream()
                .filter(friendMissing::contains).sorted()
                .map(code -> toBriefDTO(myStickerMap.get(code)))
                .collect(Collectors.toList());

        List<StickerBriefDTO> friendOfferings = friendDups.stream()
                .filter(myMissing::contains).sorted()
                .map(code -> toBriefDTO(friendStickerMap.get(code)))
                .collect(Collectors.toList());

        TradeCalculationDTO dto = new TradeCalculationDTO();
        dto.setFriendId(friend.getId());
        dto.setFriendDisplayName(friend.getDisplayName());
        dto.setFriendUserTag(friend.getUserTag());
        dto.setMyOfferings(myOfferings);
        dto.setFriendOfferings(friendOfferings);
        dto.setMaxTrades(Math.min(myOfferings.size(), friendOfferings.size()));
        return dto;
    }

    @Transactional
    public TradeProposalDTO propose(Long proposerId, Long counterpartId, CreateTradeDTO dto) {
        if (!friendshipService.areFriends(proposerId, counterpartId)) {
            throw new IllegalArgumentException("Só podes propor trocas a amigos");
        }
        User proposer = userService.getById(proposerId);
        User counterpart = userService.getById(counterpartId);

        TradeProposal trade = new TradeProposal();
        trade.setProposer(proposer);
        trade.setCounterpart(counterpart);
        trade.setProposerItems(new ArrayList<>(dto.getProposerItems()));
        trade.setCounterpartItems(new ArrayList<>(dto.getCounterpartItems()));
        trade.setCollectionId(COLLECTION_ID);
        TradeProposal saved = tradeProposalRepository.save(trade);

        String items = String.join(", ", dto.getProposerItems());
        String wants = String.join(", ", dto.getCounterpartItems());
        String content = proposer.getDisplayName() + " quer fazer " + dto.getProposerItems().size()
                + " troca(s) contigo! Oferece: " + items + " | Quer os teus: " + wants;
        messageService.sendInternal(proposerId, counterpartId, content, MessageType.TRADE_PROPOSAL, saved.getId());

        return toDTO(saved);
    }

    @Transactional
    public TradeProposalDTO respond(Long tradeId, Long counterpartId, RespondTradeDTO dto) {
        TradeProposal trade = tradeProposalRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Troca não encontrada"));
        if (!trade.getCounterpart().getId().equals(counterpartId)) {
            throw new IllegalArgumentException("Sem permissão");
        }
        if (trade.getStatus() != TradeStatus.PENDING_COUNTERPART) {
            throw new IllegalArgumentException("Troca não está à espera de resposta");
        }

        Long proposerId = trade.getProposer().getId();
        String counterpartName = trade.getCounterpart().getDisplayName();

        if (!dto.isAccept()) {
            trade.setStatus(TradeStatus.REJECTED);
            tradeProposalRepository.save(trade);
            messageService.sendInternal(counterpartId, proposerId,
                    counterpartName + " recusou a tua proposta de troca.", MessageType.TRADE_REJECTED, tradeId);
            return toDTO(trade);
        }

        List<String> finalCounterpartItems = (dto.getCounterpartItems() != null && !dto.getCounterpartItems().isEmpty())
                ? new ArrayList<>(dto.getCounterpartItems())
                : trade.getCounterpartItems();
        trade.setCounterpartItems(finalCounterpartItems);
        trade.setStatus(TradeStatus.PENDING_PROPOSER);
        tradeProposalRepository.save(trade);

        String offers = String.join(", ", finalCounterpartItems);
        String wants = String.join(", ", trade.getProposerItems());
        String content = counterpartName + " aceitou a troca! Dará: " + offers + " | Quer receber: " + wants + ". Confirmas?";
        messageService.sendInternal(counterpartId, proposerId, content, MessageType.TRADE_RESPONSE, tradeId);

        return toDTO(trade);
    }

    @Transactional
    public TradeProposalDTO confirm(Long tradeId, Long proposerId, boolean accept) {
        TradeProposal trade = tradeProposalRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Troca não encontrada"));
        if (!trade.getProposer().getId().equals(proposerId)) {
            throw new IllegalArgumentException("Sem permissão");
        }
        if (trade.getStatus() != TradeStatus.PENDING_PROPOSER) {
            throw new IllegalArgumentException("Troca não está à espera de confirmação");
        }

        Long counterpartId = trade.getCounterpart().getId();
        String proposerName = trade.getProposer().getDisplayName();

        if (!accept) {
            trade.setStatus(TradeStatus.REJECTED);
            tradeProposalRepository.save(trade);
            messageService.sendInternal(proposerId, counterpartId,
                    proposerName + " cancelou a troca.", MessageType.TRADE_REJECTED, tradeId);
            return toDTO(trade);
        }

        trade.setStatus(TradeStatus.CONFIRMED);
        tradeProposalRepository.save(trade);

        String proposerOffers = String.join(", ", trade.getProposerItems());
        String counterpartOffers = String.join(", ", trade.getCounterpartItems());
        String content = "Troca confirmada! " + proposerName + " dará " + proposerOffers
                + " e receberá " + counterpartOffers + ". Fica pendente ate ser efectuada em mao.";
        messageService.sendInternal(proposerId, counterpartId, content, MessageType.TRADE_CONFIRMED, tradeId);

        return toDTO(trade);
    }

    @Transactional
    public TradeProposalDTO complete(Long tradeId, Long userId) {
        TradeProposal trade = tradeProposalRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Troca não encontrada"));

        boolean isProposer = trade.getProposer().getId().equals(userId);
        boolean isCounterpart = trade.getCounterpart().getId().equals(userId);
        if (!isProposer && !isCounterpart) throw new IllegalArgumentException("Sem permissão");
        if (trade.getStatus() != TradeStatus.CONFIRMED) throw new IllegalArgumentException("Troca não está confirmada");

        User proposer = userService.getById(trade.getProposer().getId());
        User counterpart = userService.getById(trade.getCounterpart().getId());

        for (String code : trade.getProposerItems()) {
            transferDuplicate(proposer, counterpart, code);
        }
        for (String code : trade.getCounterpartItems()) {
            transferDuplicate(counterpart, proposer, code);
        }

        trade.setStatus(TradeStatus.COMPLETED);
        return toDTO(tradeProposalRepository.save(trade));
    }

    public TradeProposalDTO getTrade(Long tradeId, Long userId) {
        TradeProposal trade = tradeProposalRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Troca não encontrada"));
        boolean isParticipant = trade.getProposer().getId().equals(userId) || trade.getCounterpart().getId().equals(userId);
        if (!isParticipant) throw new IllegalArgumentException("Sem permissão");
        return toDTO(trade);
    }

    public List<TradeProposalDTO> getMyTrades(Long userId) {
        return tradeProposalRepository.findByParticipant(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    private void transferDuplicate(User giver, User receiver, String code) {
        var giverDup = userDuplicateRepository.findByUserAndSticker_Code(giver, code)
                .orElseThrow(() -> new IllegalStateException(giver.getDisplayName() + " não tem " + code + " duplicado"));

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

    private TradeProposalDTO toDTO(TradeProposal t) {
        TradeProposalDTO dto = new TradeProposalDTO();
        dto.setId(t.getId());
        dto.setProposerId(t.getProposer().getId());
        dto.setProposerName(t.getProposer().getDisplayName());
        dto.setProposerUserTag(t.getProposer().getUserTag());
        dto.setCounterpartId(t.getCounterpart().getId());
        dto.setCounterpartName(t.getCounterpart().getDisplayName());
        dto.setCounterpartUserTag(t.getCounterpart().getUserTag());
        dto.setProposerItems(t.getProposerItems().stream().map(this::toBriefDTOByCode).collect(Collectors.toList()));
        dto.setCounterpartItems(t.getCounterpartItems().stream().map(this::toBriefDTOByCode).collect(Collectors.toList()));
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
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

    private StickerBriefDTO toBriefDTOByCode(String code) {
        try {
            Sticker s = stickerService.getByCode(code);
            StickerBriefDTO dto = new StickerBriefDTO();
            dto.setCode(s.getCode());
            dto.setNumber(s.getNumber());
            dto.setPlayerName(s.getPlayerName());
            dto.setTeamName(s.getTeamName());
            dto.setTeamInitial(s.getTeamInitial());
            return dto;
        } catch (Exception e) {
            StickerBriefDTO dto = new StickerBriefDTO();
            dto.setCode(code);
            return dto;
        }
    }
}
