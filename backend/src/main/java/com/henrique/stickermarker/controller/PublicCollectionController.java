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

@RestController
@RequiredArgsConstructor
public class PublicCollectionController {

    private final UserRepository userRepository;
    private final CollectionService collectionService;
    private final FriendshipService friendshipService;
    private final UserService userService;

    @GetMapping("/users/{userTag}/collection/{collectionId}/stickers")
    public List<CollectionStickerStatusDTO> getStickers(
            @PathVariable String userTag,
            @PathVariable Long collectionId,
            Authentication auth) {
        User target = resolveAndCheckAccess(userTag, auth);
        return collectionService.getStickersWithStatus(target, collectionId);
    }

    @GetMapping("/users/{userTag}/collection/{collectionId}/progress")
    public CollectionProgressDTO getProgress(
            @PathVariable String userTag,
            @PathVariable Long collectionId,
            Authentication auth) {
        User target = resolveAndCheckAccess(userTag, auth);
        return collectionService.getProgress(target, collectionId);
    }

    private User resolveAndCheckAccess(String userTag, Authentication auth) {
        User target = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Long viewerId = (Long) auth.getDetails();

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
