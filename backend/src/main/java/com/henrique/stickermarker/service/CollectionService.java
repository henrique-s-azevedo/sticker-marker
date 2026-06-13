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

/**
 * Business logic for sticker collections, including catalog queries, per-user progress,
 * and per-sticker ownership status.
 *
 * <p>A {@link Collection} is a global album template shared by all users — it is not
 * user-specific. User ownership is tracked via {@link com.henrique.stickermarker.model.UserSticker}
 * and {@link com.henrique.stickermarker.model.UserDuplicate}.</p>
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

    /**
     * Creates a new collection (album template). Only used during initial data setup.
     *
     * @param dto the creation payload (name, totalStickers, totalPages)
     * @return the persisted collection as a DTO
     */
    public CollectionDTO create(CollectionCreateDTO dto) {
        Collection c = new Collection();
        c.setName(dto.getName());
        c.setTotalStickers(dto.getTotalStickers());
        c.setTotalPages(dto.getTotalPages());
        return toDTO(collectionRepository.save(c));
    }

    /**
     * Returns all available collections (albums).
     *
     * @return list of all collection DTOs
     */
    public List<CollectionDTO> getAll() {
        return collectionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Returns a single collection by its ID.
     *
     * @param id the collection ID
     * @return the collection DTO
     * @throws RuntimeException if the collection does not exist (mapped to 404)
     */
    public CollectionDTO getById(Long id) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        return toDTO(c);
    }

    /**
     * Returns a compact summary of all stickers in a collection, without ownership context.
     *
     * @param collectionId the collection to list
     * @return list of sticker summaries (code, number, page, team, player)
     */
    public List<StickerSummaryDTO> getStickersByCollection(Long collectionId) {
        List<Sticker> stickers = stickerRepository.findByCollection_Id(collectionId);
        return stickers.stream()
                .map(this::toStickerSummaryDTO)
                .toList();
    }

    /**
     * Calculates the user's completion progress for a collection.
     * Duplicates are summed across all sticker codes, not counted as distinct stickers.
     *
     * @param user         the user whose progress to calculate
     * @param collectionId the target collection
     * @return progress DTO with owned/missing/duplicate counts and percentage
     * @throws RuntimeException if the collection does not exist
     */
    public CollectionProgressDTO getProgress(User user, Long collectionId) {
        Collection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        int total = c.getTotalStickers();
        long owned = userStickerRepository.countByUserAndSticker_Collection_Id(user, collectionId);
        long duplicates = userDuplicateRepository.findByUserAndSticker_Collection_Id(user, collectionId)
                .stream().mapToLong(ud -> ud.getQuantity()).sum();
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

    /**
     * Returns every sticker in a collection annotated with its ownership status for the user.
     * Status priority: DUPLICATE takes precedence over OWNED (a user who marked extras
     * is shown as DUPLICATE, not just OWNED).
     *
     * <p>Two bulk queries are issued (owned codes and duplicate quantities) to avoid N+1 lookups.</p>
     *
     * @param user         the user whose inventory to check
     * @param collectionId the collection to query
     * @return list of all stickers with OWNED, DUPLICATE, or MISSING status
     * @throws RuntimeException if the collection does not exist
     */
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

    /**
     * Maps a sticker to its status DTO, applying the DUPLICATE → OWNED → MISSING priority.
     *
     * @param s                  the sticker to map
     * @param ownedCodes         set of codes the user owns
     * @param duplicateQuantities map of code → duplicate quantity
     * @return the status DTO
     */
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
