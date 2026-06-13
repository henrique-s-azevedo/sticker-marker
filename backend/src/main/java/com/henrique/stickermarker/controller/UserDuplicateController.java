package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserDuplicateCreateDTO;
import com.henrique.stickermarker.dto.UserDuplicateDTO;
import com.henrique.stickermarker.dto.UserDuplicateUpdateDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserDuplicateService;
import com.henrique.stickermarker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing the authenticated user's duplicate sticker inventory.
 *
 * <p>All endpoints under {@code /me/duplicates} require a valid JWT. Duplicates represent
 * spare copies available for trading or selling, tracked separately from base ownership
 * ({@link UserStickerController}).</p>
 */
@RestController
@RequestMapping("/me/duplicates")
@RequiredArgsConstructor
public class UserDuplicateController {

    private final UserDuplicateService duplicateService;
    private final UserService userService;

    /**
     * Records a new duplicate sticker for the authenticated user.
     *
     * @param authentication the security context; userId extracted via {@code currentUser()}
     * @param dto            the sticker code and initial quantity
     * @return the created duplicate record
     */
    @PostMapping
    public UserDuplicateDTO createDuplicate(Authentication authentication, @RequestBody UserDuplicateCreateDTO dto) {
        return duplicateService.createDuplicate(currentUser(authentication), dto);
    }

    /**
     * Returns all duplicate sticker records for the authenticated user across all collections.
     *
     * @param authentication the security context
     * @return list of duplicate sticker records with quantities
     */
    @GetMapping
    public List<UserDuplicateDTO> getDuplicates(Authentication authentication) {
        return duplicateService.getUserDuplicates(currentUser(authentication));
    }

    /**
     * Updates the quantity of a specific duplicate sticker.
     *
     * @param authentication the security context
     * @param stickerCode    the code of the sticker to update
     * @param dto            the new quantity
     * @return the updated duplicate record
     */
    @PutMapping("/{stickerCode}")
    public UserDuplicateDTO updateDuplicate(
            Authentication authentication,
            @PathVariable String stickerCode,
            @RequestBody UserDuplicateUpdateDTO dto
    ) {
        return duplicateService.updateDuplicate(currentUser(authentication), stickerCode, dto);
    }

    /**
     * Removes a duplicate sticker record entirely. Called when the user has zero
     * remaining copies of a sticker.
     *
     * @param authentication the security context
     * @param stickerCode    the code of the sticker to remove
     */
    @DeleteMapping("/{stickerCode}")
    public void deleteDuplicate(Authentication authentication, @PathVariable String stickerCode) {
        duplicateService.deleteDuplicate(currentUser(authentication), stickerCode);
    }

    /** Resolves the authenticated user entity from the JWT details. Centralised to avoid repeating the cast. */
    private User currentUser(Authentication authentication) {
        return userService.getById((Long) authentication.getDetails());
    }
}
