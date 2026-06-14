package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.FriendRequestDTO;
import com.henrique.stickermarker.dto.InviteCodeResponseDTO;
import com.henrique.stickermarker.model.InviteCode;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.InviteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Manages invite codes that allow users to add friends without knowing their email or tag.
 *
 * <p>Each user may have at most one active code at a time. Codes expire after 30 days.
 * {@link #getOrGenerateCode} returns the existing active code or creates a new one,
 * deactivating the previous code in the process.</p>
 *
 * <p>Accepting an invite creates a friend request (not an immediate friendship).
 * The invite code is not consumed on acceptance — it remains active so the creator
 * can share it with multiple people before it expires.</p>
 */
@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Returns the user's active, non-expired invite code, or generates a new one.
     * Side effect: if no valid code exists, deactivates any stale active code before creating a new one.
     *
     * @param userId the user requesting their invite code
     * @return the active invite code DTO with the shareable URL
     */
    public InviteCodeResponseDTO getOrGenerateCode(Long userId) {
        User creator = userService.getById(userId);

        InviteCode invite = inviteCodeRepository.findByCreatorIdAndActiveTrue(userId)
                .filter(c -> c.getExpiresAt().isAfter(Instant.now()))
                .orElseGet(() -> createNewCode(creator));

        return toDTO(invite);
    }

    /**
     * Accepts an invite code and sends a friend request from the accepting user to the code's creator.
     * The code is not consumed — it remains usable until it expires.
     *
     * @param code            the invite code string
     * @param acceptingUserId the user accepting the invite
     * @return the created friend request DTO (with PENDING status)
     * @throws IllegalArgumentException if the code is invalid, inactive, expired, or the user tries
     *                                  to accept their own code
     */
    public FriendRequestDTO acceptInvite(String code, Long acceptingUserId) {
        InviteCode invite = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

        if (!invite.isActive()) throw new IllegalArgumentException("Inactive code");
        if (invite.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Expired code");
        if (invite.getCreator().getId().equals(acceptingUserId)) {
            throw new IllegalArgumentException("You cannot accept your own invite");
        }

        return friendshipService.sendRequest(acceptingUserId, invite.getCreator().getId());
    }

    /**
     * Creates a new invite code for the given user, deactivating any previously active code.
     * Codes are UUIDs with hyphens stripped (32-character hex string), valid for 30 days.
     */
    private InviteCode createNewCode(User creator) {
        inviteCodeRepository.findByCreatorIdAndActiveTrue(creator.getId())
                .ifPresent(old -> {
                    old.setActive(false);
                    inviteCodeRepository.save(old);
                });

        InviteCode invite = new InviteCode();
        invite.setCode(UUID.randomUUID().toString().replace("-", ""));
        invite.setCreator(creator);
        invite.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        return inviteCodeRepository.save(invite);
    }

    private InviteCodeResponseDTO toDTO(InviteCode invite) {
        InviteCodeResponseDTO dto = new InviteCodeResponseDTO();
        dto.setCode(invite.getCode());
        dto.setInviteUrl(frontendUrl + "/invite/" + invite.getCode());
        dto.setExpiresAt(invite.getExpiresAt());
        return dto;
    }
}
