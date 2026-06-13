package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.ChangePasswordDTO;
import com.henrique.stickermarker.dto.UpdateVisibilityDTO;
import com.henrique.stickermarker.dto.UserProfileDTO;
import com.henrique.stickermarker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the authenticated user's profile settings.
 *
 * <p>All endpoints require a valid JWT and only operate on the authenticated user's
 * own profile. The change-password flow is a two-step process: first request a
 * verification code sent to the registered email, then submit the code with the new password.</p>
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * Returns the authenticated user's profile data (display name, tag, visibility setting).
     *
     * @param auth the security context
     * @return the user's profile DTO
     */
    @GetMapping("/me/profile")
    public UserProfileDTO getProfile(Authentication auth) {
        return userService.getProfile(userId(auth));
    }

    /**
     * Updates the collection visibility setting for the authenticated user.
     *
     * @param dto  the new visibility level ({@code PUBLIC}, {@code FRIENDS_ONLY}, or {@code PRIVATE})
     * @param auth the security context
     * @return 204 No Content on success
     */
    @PutMapping("/me/collection/visibility")
    public ResponseEntity<Void> updateVisibility(
            @RequestBody @Valid UpdateVisibilityDTO dto,
            Authentication auth) {
        userService.updateVisibility(userId(auth), dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Step 1 of the change-password flow: triggers sending a one-time verification code
     * to the user's registered email address via Brevo.
     * Side effect: creates an {@link com.henrique.stickermarker.model.EmailVerificationCode} record
     * and sends the code by email.
     *
     * @param auth the security context
     * @return 204 No Content on success; the code is delivered via email
     */
    @PostMapping("/me/change-password/send-code")
    public ResponseEntity<Void> sendPasswordChangeCode(Authentication auth) {
        userService.sendPasswordChangeCode(userId(auth));
        return ResponseEntity.noContent().build();
    }

    /**
     * Step 2 of the change-password flow: verifies the submitted code and updates the password.
     * Side effect: marks the verification code as used and updates the password hash.
     * Rejects the request if the code is expired, already used, or does not match.
     *
     * @param dto  the verification code and new password
     * @param auth the security context
     * @return 204 No Content on success
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordDTO dto,
            Authentication auth) {
        userService.changePassword(userId(auth), dto);
        return ResponseEntity.noContent().build();
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
