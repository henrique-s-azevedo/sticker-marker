package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.UserStickerCreateDTO;
import com.henrique.stickermarker.dto.UserStickerDTO;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import com.henrique.stickermarker.repository.UserStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages a user's owned sticker inventory (non-duplicate stickers).
 *
 * <p>Ownership is tracked by a {@link UserSticker} record per (user, sticker) pair.
 * The database does not enforce a unique constraint on this pair — uniqueness is enforced
 * here in the service layer. Duplicates are managed separately via {@link UserDuplicateService}.</p>
 */
@Service
@RequiredArgsConstructor
public class UserStickerService {

    private final UserStickerRepository userStickerRepository;
    private final StickerService stickerService;

    /**
     * Adds a sticker to the user's collection by sticker code.
     * Enforces at most one owned record per (user, sticker) pair.
     *
     * @param user the owner
     * @param dto  the sticker code to add
     * @return the created ownership record as a DTO
     * @throws RuntimeException if the user already owns this sticker
     * @throws RuntimeException if the sticker code does not exist in the catalog
     */
    public UserStickerDTO addStickerToUser(User user, UserStickerCreateDTO dto) {

        String code = dto.getStickerCode();

        var existing = userStickerRepository.findByUserAndSticker_Code(user, code);
        if (existing.isPresent()) {
            throw new RuntimeException("User already has this sticker");
        }

        Sticker sticker = stickerService.getByCode(code);

        UserSticker userSticker = new UserSticker();
        userSticker.setUser(user);
        userSticker.setSticker(sticker);

        UserSticker saved = userStickerRepository.save(userSticker);

        return toDTO(saved);
    }

    /**
     * Returns all stickers owned by the user across all collections.
     *
     * @param user the owner
     * @return list of owned sticker DTOs
     */
    public List<UserStickerDTO> getUserStickers(User user) {
        return userStickerRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Removes a sticker from the user's collection by code.
     * Does not affect the user's duplicate records for the same sticker.
     *
     * @param user        the owner
     * @param stickerCode the code of the sticker to remove
     */
    @Transactional
    public void removeStickerFromUser(User user, String stickerCode) {
        userStickerRepository.deleteByUserAndSticker_Code(user, stickerCode);
    }

    private UserStickerDTO toDTO(UserSticker us) {
        UserStickerDTO dto = new UserStickerDTO();
        dto.setId(us.getId());
        dto.setStickerCode(us.getSticker().getCode());
        dto.setStickerNumber(us.getSticker().getNumber());
        dto.setPageNumber(us.getSticker().getPageNumber());
        dto.setTeamInitial(us.getSticker().getTeamInitial());
        dto.setPlayerName(us.getSticker().getPlayerName());
        dto.setCollectionId(us.getSticker().getCollection().getId());
        return dto;
    }
}
