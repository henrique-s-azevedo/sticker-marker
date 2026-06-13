package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.FriendRequestDTO;
import com.henrique.stickermarker.dto.InviteCodeResponseDTO;
import com.henrique.stickermarker.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for shareable friend invite codes.
 *
 * <p>Invite codes allow users to add friends by sharing a link, without knowing the
 * target's email or tag. Both endpoints require a valid JWT.</p>
 */
@RestController
@RequiredArgsConstructor
public class InviteController {

    private final InviteCodeService inviteCodeService;

    /**
     * Returns the authenticated user's active invite code, generating a new one if none exists.
     * Side effect: if no active code exists, a new one is persisted with a default expiry.
     *
     * @param auth the security context
     * @return the user's current active invite code and its expiry date
     */
    @GetMapping("/me/invite")
    public InviteCodeResponseDTO getMyInvite(Authentication auth) {
        return inviteCodeService.getOrGenerateCode(userId(auth));
    }

    /**
     * Accepts an invite code, creating a friend request from the authenticated user
     * to the code's creator.
     * Side effect: creates a {@link com.henrique.stickermarker.model.Friendship} record
     * with {@code PENDING} status between the accepting user and the code creator.
     *
     * @param code the invite code string
     * @param auth the security context; the authenticated user becomes the requester
     * @return the created friend request DTO
     */
    @PostMapping("/invite/{code}/accept")
    public FriendRequestDTO acceptInvite(@PathVariable String code, Authentication auth) {
        return inviteCodeService.acceptInvite(code, userId(auth));
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
