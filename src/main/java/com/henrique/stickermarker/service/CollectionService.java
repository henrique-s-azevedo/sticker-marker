package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.CollectionRepository;
import com.henrique.stickermarker.repository.StickerRepository;
import com.henrique.stickermarker.repository.UserDuplicateRepository;
import com.henrique.stickermarker.repository.UserStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

    public CollectionDTO create(CollectionCreateDTO dto) {
        Collection c = new Collection();
        c.setName(dto.getName());
        c.setTotalStickers(dto.getTotalStickers());
        c.setTotalPages(dto.getTotalPages());
        return toDTO(collectionRepository.save(c));
    }

    public List<CollectionDTO> getAll() {
        return collectionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public CollectionDTO getById(Long id) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        return toDTO(c);
    }

    public List<StickerSummaryDTO> getStickersByCollection(Long collectionId) {
        List<Sticker> stickers = stickerRepository.findByCollection_Id(collectionId);
        return stickers.stream()
                .map(this::toStickerSummaryDTO)
                .toList();
    }

    public CollectionProgressDTO getProgress(User user, Long collectionId) {
        Collection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        int total = c.getTotalStickers();
        long owned = userStickerRepository.countByUserAndSticker_Collection_Id(user, collectionId);
        long duplicates = userDuplicateRepository.findByUserAndSticker_Collection_Id(user, collectionId).size();
        int missing = total - (int) owned;
        double percentage = total == 0 ? 0.0 : (owned * 100.0) / total;

        CollectionProgressDTO dto = new CollectionProgressDTO();
        dto.setCollectionId(c.getId());
        dto.setCollectionName(c.getName());
        dto.setTotal(total);
        dto.setOwned(owned);
        dto.setMissing(missing);
        dto.setDuplicates(duplicates);
        dto.setPercentage(percentage);

        return dto;
    }

    public List<CollectionStickerStatusDTO> getStickersWithStatus(User user, Long collectionId) {
        if (!collectionRepository.existsById(collectionId)) {
            throw new RuntimeException("Collection not found");
        }

        Set<String> ownedCodes = userStickerRepository
                .findByUserAndSticker_Collection_Id(user, collectionId)
                .stream()
                .map(us -> us.getSticker().getCode())
                .collect(Collectors.toSet());

        Map<String, Integer> duplicateQuantities = userDuplicateRepository
                .findByUserAndSticker_Collection_Id(user, collectionId)
                .stream()
                .collect(Collectors.toMap(ud -> ud.getSticker().getCode(), ud -> ud.getQuantity()));

        return stickerRepository.findByCollection_Id(collectionId)
                .stream()
                .map(s -> toStatusDTO(s, ownedCodes, duplicateQuantities))
                .toList();
    }

    private CollectionStickerStatusDTO toStatusDTO(Sticker s, Set<String> ownedCodes, Map<String, Integer> duplicateQuantities) {
        CollectionStickerStatusDTO dto = new CollectionStickerStatusDTO();
        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setNumber(s.getNumber());
        dto.setPlayerName(s.getPlayerName());
        dto.setTeamName(s.getTeamName());
        dto.setTeamInitial(s.getTeamInitial());
        dto.setPageNumber(s.getPageNumber());

        if (duplicateQuantities.containsKey(s.getCode())) {
            dto.setStatus(StickerStatus.DUPLICATE);
            dto.setDuplicateQuantity(duplicateQuantities.get(s.getCode()));
        } else if (ownedCodes.contains(s.getCode())) {
            dto.setStatus(StickerStatus.OWNED);
        } else {
            dto.setStatus(StickerStatus.MISSING);
        }

        return dto;
    }

    private CollectionDTO toDTO(Collection c) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setTotalStickers(c.getTotalStickers());
        dto.setTotalPages(c.getTotalPages());
        return dto;
    }

    private StickerSummaryDTO toStickerSummaryDTO(Sticker s) {
        StickerSummaryDTO dto = new StickerSummaryDTO();
        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setNumber(s.getNumber());
        dto.setPage(s.getPageNumber());
        dto.setTeamInitial(s.getTeamInitial());
        dto.setPlayerName(s.getPlayerName());
        return dto;
    }
}
