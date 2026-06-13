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

/**
 * Controller for friendship management between users.
 *
 * <p>Handles all operations under {@code /me/friends}, {@code /me/friend-requests},
 * and {@code /users/search}. All endpoints require a valid JWT.
 * The authenticated user ID is extracted from {@code auth.getDetails()} via the
 * private {@code userId()} helper.</p>
 *
 * <p>Friend requests can be sent by email or by public user tag.
 * The minimum query length of 2 characters on search is enforced at the controller level
 * to prevent overly broad database queries.</p>
 */
@RestController
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * Returns the accepted friends list for the authenticated user.
     *
     * @param auth the security context
     * @return list of accepted friends with basic profile info
     */
    @GetMapping("/me/friends")
    public List<FriendDTO> getFriends(Authentication auth) {
        return friendshipService.getFriends(userId(auth));
    }

    /**
     * Removes an accepted friendship between the authenticated user and the specified friend.
     * Both directions of the friendship record are resolved by the service.
     *
     * @param friendId the ID of the friend to remove
     * @param auth     the security context
     * @return 204 No Content on success
     */
    @DeleteMapping("/me/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendId, Authentication auth) {
        friendshipService.removeFriend(userId(auth), friendId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns all pending friend requests received by the authenticated user.
     *
     * @param auth the security context
     * @return list of incoming pending friend requests
     */
    @GetMapping("/me/friend-requests")
    public List<FriendRequestDTO> getPendingRequests(Authentication auth) {
        return friendshipService.getPendingReceived(userId(auth));
    }

    /**
     * Returns all friend requests sent by the authenticated user that are still pending.
     *
     * @param auth the security context
     * @return list of outgoing pending friend requests
     */
    @GetMapping("/me/friend-requests/sent")
    public List<FriendRequestDTO> getSentRequests(Authentication auth) {
        return friendshipService.getPendingSent(userId(auth));
    }

    /**
     * Returns the count of pending received friend requests.
     * Used to populate notification badges in the UI without loading the full list.
     *
     * @param auth the security context
     * @return map with key {@code "count"} and the number of pending requests
     */
    @GetMapping("/me/friend-requests/count")
    public Map<String, Long> getPendingCount(Authentication auth) {
        long count = friendshipService.getPendingReceived(userId(auth)).size();
        return Map.of("count", count);
    }

    /**
     * Sends a friend request by the target user's email address.
     *
     * @param dto  the target user's email
     * @param auth the security context
     * @return the created friend request record
     */
    @PostMapping("/me/friends/request/email")
    public FriendRequestDTO addByEmail(
            @RequestBody @Valid AddFriendByEmailDTO dto,
            Authentication auth) {
        return friendshipService.sendRequestByEmail(userId(auth), dto);
    }

    /**
     * Sends a friend request by the target user's public tag.
     *
     * @param dto  the target user's tag
     * @param auth the security context
     * @return the created friend request record
     */
    @PostMapping("/me/friends/request/tag")
    public FriendRequestDTO addByTag(
            @RequestBody @Valid AddFriendByTagDTO dto,
            Authentication auth) {
        return friendshipService.sendRequestByTag(userId(auth), dto);
    }

    /**
     * Accepts a pending friend request addressed to the authenticated user.
     *
     * @param requestId the ID of the friendship record to accept
     * @param auth      the security context; used to verify the request is addressed to this user
     * @return the updated friendship record with {@code ACCEPTED} status
     */
    @PostMapping("/me/friend-requests/{requestId}/accept")
    public FriendRequestDTO accept(@PathVariable Long requestId, Authentication auth) {
        return friendshipService.acceptRequest(requestId, userId(auth));
    }

    /**
     * Rejects a pending friend request addressed to the authenticated user.
     *
     * @param requestId the ID of the friendship record to reject
     * @param auth      the security context; used to verify the request is addressed to this user
     * @return 204 No Content on success
     */
    @PostMapping("/me/friend-requests/{requestId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long requestId, Authentication auth) {
        friendshipService.rejectRequest(requestId, userId(auth));
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches for users by display name or tag. Minimum query length of 2 characters
     * is enforced here to prevent full-table scans from single-character queries.
     *
     * @param q    the search term (matched as substring against display name and tag)
     * @param auth the security context; the current user is excluded from results by the service
     * @return list of matching users with basic profile info
     */
    @GetMapping("/users/search")
    public List<UserSearchResultDTO> search(
            @RequestParam String q,
            Authentication auth) {
        if (q == null || q.trim().length() < 2) return List.of();
        return friendshipService.searchUsers(userId(auth), q.trim());
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
