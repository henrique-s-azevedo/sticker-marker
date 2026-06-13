package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.CollectionProgressDTO;
import com.henrique.stickermarker.dto.CollectionStickerStatusDTO;
import com.henrique.stickermarker.model.CollectionVisibility;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.UserRepository;
import com.henrique.stickermarker.service.CollectionService;
import com.henrique.stickermarker.service.FriendshipService;
import com.henrique.stickermarker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for viewing another user's sticker collection with visibility enforcement.
 *
 * <p>Endpoints use the target user's public {@code userTag} rather than their numeric ID
 * to avoid exposing internal IDs in shareable URLs.</p>
 *
 * <p>Access is controlled by the target's {@link CollectionVisibility} setting:
 * <ul>
 *   <li>{@code PUBLIC} — any authenticated user may view.</li>
 *   <li>{@code FRIENDS_ONLY} — only users with an accepted friendship may view.</li>
 *   <li>{@code PRIVATE} — only the owner may view.</li>
 * </ul>
 * Owners always have access to their own collection regardless of visibility setting.</p>
 */
@RestController
@RequiredArgsConstructor
public class PublicCollectionController {

    private final UserRepository userRepository;
    private final CollectionService collectionService;
    private final FriendshipService friendshipService;
    private final UserService userService;

    /**
     * Returns the sticker status list for another user's collection, subject to visibility rules.
     *
     * @param userTag      the public tag of the target user
     * @param collectionId the collection to view
     * @param auth         the security context; viewer ID extracted from JWT details
     * @return the target user's sticker status list for the given collection
     * @throws RuntimeException         if the target user does not exist (mapped to 404)
     * @throws IllegalArgumentException if the viewer does not have permission to view the collection (mapped to 400)
     */
    @GetMapping("/users/{userTag}/collection/{collectionId}/stickers")
    public List<CollectionStickerStatusDTO> getStickers(
            @PathVariable String userTag,
            @PathVariable Long collectionId,
            Authentication auth) {
        User target = resolveAndCheckAccess(userTag, auth);
        return collectionService.getStickersWithStatus(target, collectionId);
    }

    /**
     * Returns the completion progress for another user's collection, subject to visibility rules.
     *
     * @param userTag      the public tag of the target user
     * @param collectionId the collection to view
     * @param auth         the security context
     * @return progress DTO for the target user's collection
     * @throws RuntimeException         if the target user does not exist (mapped to 404)
     * @throws IllegalArgumentException if the viewer does not have permission to view the collection (mapped to 400)
     */
    @GetMapping("/users/{userTag}/collection/{collectionId}/progress")
    public CollectionProgressDTO getProgress(
            @PathVariable String userTag,
            @PathVariable Long collectionId,
            Authentication auth) {
        User target = resolveAndCheckAccess(userTag, auth);
        return collectionService.getProgress(target, collectionId);
    }

    /**
     * Resolves the target user by tag and enforces visibility access rules.
     * Owners bypass the visibility check to always allow self-access.
     *
     * @param userTag the target user's public tag
     * @param auth    the security context of the requesting user
     * @return the resolved target {@link User}
     * @throws RuntimeException         if the user tag does not exist
     * @throws IllegalArgumentException if the viewer's access level is insufficient
     */
    private User resolveAndCheckAccess(String userTag, Authentication auth) {
        User target = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Long viewerId = (Long) auth.getDetails();

        // Owners always have access to their own collection
        if (target.getId().equals(viewerId)) return target;

        CollectionVisibility vis = target.getCollectionVisibility();
        if (vis == CollectionVisibility.PRIVATE) {
            throw new IllegalArgumentException("Coleção privada");
        }
        if (vis == CollectionVisibility.FRIENDS_ONLY) {
            if (!friendshipService.areFriends(viewerId, target.getId())) {
                throw new IllegalArgumentException("Apenas amigos podem ver esta coleção");
            }
        }
        return target;
    }
}
