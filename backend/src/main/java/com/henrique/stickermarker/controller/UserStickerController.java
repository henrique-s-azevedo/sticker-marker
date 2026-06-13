package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserStickerCreateDTO;
import com.henrique.stickermarker.dto.UserStickerDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserService;
import com.henrique.stickermarker.service.UserStickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing the authenticated user's sticker ownership.
 *
 * <p>All endpoints under {@code /me/stickers} require a valid JWT.
 * Operations are always scoped to the authenticated user — it is not possible
 * to modify another user's sticker list through this controller.</p>
 */
@RestController
@RequestMapping("/me/stickers")
@RequiredArgsConstructor
public class UserStickerController {

    private final UserStickerService userStickerService;
    private final UserService userService;

    /**
     * Marks a sticker as owned by the authenticated user.
     *
     * @param authentication the security context; userId extracted via {@code currentUser()}
     * @param dto            the sticker code to mark as owned
     * @return the created ownership record
     */
    @PostMapping
    public UserStickerDTO addSticker(Authentication authentication, @RequestBody UserStickerCreateDTO dto) {
        return userStickerService.addStickerToUser(currentUser(authentication), dto);
    }

    /**
     * Returns all stickers owned by the authenticated user across all collections.
     *
     * @param authentication the security context
     * @return list of owned sticker records
     */
    @GetMapping
    public List<UserStickerDTO> getUserStickers(Authentication authentication) {
        return userStickerService.getUserStickers(currentUser(authentication));
    }

    /**
     * Removes a sticker from the authenticated user's owned list.
     *
     * @param authentication the security context
     * @param stickerCode    the code of the sticker to unmark
     */
    @DeleteMapping("/{stickerCode}")
    public void removeSticker(Authentication authentication, @PathVariable String stickerCode) {
        userStickerService.removeStickerFromUser(currentUser(authentication), stickerCode);
    }

    /** Resolves the authenticated user entity from the JWT details. Centralised to avoid repeating the cast. */
    private User currentUser(Authentication authentication) {
        return userService.getById((Long) authentication.getDetails());
    }
}
