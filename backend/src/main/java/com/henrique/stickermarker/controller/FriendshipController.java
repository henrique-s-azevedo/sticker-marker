package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.service.FriendshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/me/friends")
    public List<FriendDTO> getFriends(Authentication auth) {
        return friendshipService.getFriends(userId(auth));
    }

    @DeleteMapping("/me/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendId, Authentication auth) {
        friendshipService.removeFriend(userId(auth), friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/friend-requests")
    public List<FriendRequestDTO> getPendingRequests(Authentication auth) {
        return friendshipService.getPendingReceived(userId(auth));
    }

    @GetMapping("/me/friend-requests/sent")
    public List<FriendRequestDTO> getSentRequests(Authentication auth) {
        return friendshipService.getPendingSent(userId(auth));
    }

    @GetMapping("/me/friend-requests/count")
    public Map<String, Long> getPendingCount(Authentication auth) {
        long count = friendshipService.getPendingReceived(userId(auth)).size();
        return Map.of("count", count);
    }

    @PostMapping("/me/friends/request/email")
    public FriendRequestDTO addByEmail(
            @RequestBody @Valid AddFriendByEmailDTO dto,
            Authentication auth) {
        return friendshipService.sendRequestByEmail(userId(auth), dto);
    }

    @PostMapping("/me/friends/request/tag")
    public FriendRequestDTO addByTag(
            @RequestBody @Valid AddFriendByTagDTO dto,
            Authentication auth) {
        return friendshipService.sendRequestByTag(userId(auth), dto);
    }

    @PostMapping("/me/friend-requests/{requestId}/accept")
    public FriendRequestDTO accept(@PathVariable Long requestId, Authentication auth) {
        return friendshipService.acceptRequest(requestId, userId(auth));
    }

    @PostMapping("/me/friend-requests/{requestId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long requestId, Authentication auth) {
        friendshipService.rejectRequest(requestId, userId(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/search")
    public List<UserSearchResultDTO> search(
            @RequestParam String q,
            Authentication auth) {
        if (q == null || q.trim().length() < 2) return List.of();
        return friendshipService.searchUsers(userId(auth), q.trim());
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
