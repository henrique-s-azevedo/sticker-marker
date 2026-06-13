package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.UserDuplicateCreateDTO;
import com.henrique.stickermarker.dto.UserDuplicateDTO;
import com.henrique.stickermarker.dto.UserDuplicateUpdateDTO;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import com.henrique.stickermarker.repository.UserDuplicateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages a user's duplicate sticker inventory — stickers the user has more than once
 * and is willing to trade or sell.
 *
 * <p>A duplicate record requires the user to already own the sticker (a {@link com.henrique.stickermarker.model.UserSticker}
 * record must exist). This prevents users from advertising duplicates they do not actually have.</p>
 *
 * <p>Setting the quantity to 0 via {@link #updateDuplicate} deletes the record rather than
 * persisting a zero-quantity entry, keeping the invariant that every stored record has quantity ≥ 1.</p>
 */
@Service
@RequiredArgsConstructor
public class UserDuplicateService {

    private final UserDuplicateRepository duplicateRepository;
    private final StickerService stickerService;
    private final UserStickerService userStickerService;

    /**
     * Creates a duplicate record for a sticker the user already owns.
     * Verifies ownership before creation to prevent phantom duplicate entries.
     *
     * @param user the owner
     * @param dto  the sticker code and initial quantity
     * @return the created duplicate record as a DTO
     * @throws RuntimeException if the user does not own the sticker
     * @throws RuntimeException if a duplicate record already exists (use PUT to update)
     */
    public UserDuplicateDTO createDuplicate(User user, UserDuplicateCreateDTO dto) {

        String code = dto.getStickerCode();

        var hasSticker = userStickerService
                .getUserStickers(user)
                .stream()
                .anyMatch(s -> s.getStickerCode().equals(code));

        if (!hasSticker) {
            throw new RuntimeException("User must own the sticker before adding duplicates");
        }

        var existing = duplicateRepository.findByUserAndSticker_Code(user, code);
        if (existing.isPresent()) {
            throw new RuntimeException("Duplicate already exists. Use PUT to update quantity.");
        }

        Sticker sticker = stickerService.getByCode(code);

        UserDuplicate duplicate = new UserDuplicate();
        duplicate.setUser(user);
        duplicate.setSticker(sticker);
        duplicate.setQuantity(dto.getQuantity());

        return toDTO(duplicateRepository.save(duplicate));
    }

    /**
     * Updates the quantity of an existing duplicate record.
     * A quantity of 0 removes the record entirely rather than storing a zero entry.
     *
     * @param user the owner
     * @param code the sticker code whose duplicate quantity to update
     * @param dto  the new quantity
     * @return the updated duplicate DTO, or {@code null} if the record was deleted (quantity = 0)
     * @throws RuntimeException if no duplicate record exists for this sticker
     */
    public UserDuplicateDTO updateDuplicate(User user, String code, UserDuplicateUpdateDTO dto) {

        var existing = duplicateRepository.findByUserAndSticker_Code(user, code)
                .orElseThrow(() -> new RuntimeException("Duplicate not found"));

        int quantity = dto.getQuantity();

        if (quantity == 0) {
            duplicateRepository.delete(existing);
            return null;
        }

        existing.setQuantity(quantity);
        return toDTO(duplicateRepository.save(existing));
    }

    /**
     * Returns all duplicate records for the user across all collections.
     *
     * @param user the owner
     * @return list of duplicate DTOs
     */
    public List<UserDuplicateDTO> getUserDuplicates(User user) {
        return duplicateRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Deletes a duplicate record by sticker code.
     *
     * @param user the owner
     * @param code the sticker code to remove
     */
    @Transactional
    public void deleteDuplicate(User user, String code) {
        duplicateRepository.deleteByUserAndSticker_Code(user, code);
    }

    private UserDuplicateDTO toDTO(UserDuplicate d) {
        UserDuplicateDTO dto = new UserDuplicateDTO();
        dto.setId(d.getId());
        dto.setStickerCode(d.getSticker().getCode());
        dto.setStickerNumber(d.getSticker().getNumber());
        dto.setTeamInitial(d.getSticker().getTeamInitial());
        dto.setPlayerName(d.getSticker().getPlayerName());
        dto.setPage(d.getSticker().getPageNumber());
        dto.setCollectionId(d.getSticker().getCollection().getId());
        dto.setQuantity(d.getQuantity());
        return dto;
    }
}
