package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.Friendship;
import com.henrique.stickermarker.model.FriendshipStatus;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.FriendshipRepository;
import com.henrique.stickermarker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the friendship lifecycle: send requests, accept/reject, remove, and search.
 *
 * <p>The {@link Friendship} model is directional (requester → addressee), but the relationship
 * is bidirectional once accepted. {@link #areFriends} checks both directions,
 * as does the repository query used by {@link #removeFriend}.</p>
 *
 * <p>REJECTED records are kept in the database for audit purposes.
 * Removed friendships (via {@link #removeFriend}) are hard-deleted.</p>
 */
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Sends a friend request to a user identified by their email address.
     *
     * @param requesterId the ID of the user sending the request
     * @param dto         the target user's email
     * @return the created friendship request as a DTO
     * @throws IllegalArgumentException if the target email does not exist or a duplicate request is found
     */
    public FriendRequestDTO sendRequestByEmail(Long requesterId, AddFriendByEmailDTO dto) {
        User addressee = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return sendRequest(requesterId, addressee.getId());
    }

    /**
     * Sends a friend request to a user identified by their userTag.
     * Accepts tags with or without the leading {@code @} prefix.
     *
     * @param requesterId the ID of the user sending the request
     * @param dto         the target user's userTag
     * @return the created friendship request as a DTO
     * @throws IllegalArgumentException if the tag does not exist or a duplicate request is found
     */
    public FriendRequestDTO sendRequestByTag(Long requesterId, AddFriendByTagDTO dto) {
        String tag = dto.getUserTag().startsWith("@") ? dto.getUserTag().substring(1) : dto.getUserTag();
        User addressee = userRepository.findByUserTag(tag)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return sendRequest(requesterId, addressee.getId());
    }

    /**
     * Core logic for sending a friend request between two user IDs.
     * Prevents self-requests and duplicate requests (whether PENDING or ACCEPTED).
     *
     * @param requesterId the ID of the requester
     * @param addresseeId the ID of the intended addressee
     * @return the created friendship request DTO with {@code PENDING} status
     * @throws IllegalArgumentException if the users are the same, already friends, or a request is already pending
     */
    public FriendRequestDTO sendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("You cannot add yourself");
        }
        User requester = userService.getById(requesterId);
        User addressee = userService.getById(addresseeId);

        friendshipRepository.findBetweenUsers(requesterId, addresseeId).ifPresent(f -> {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) throw new IllegalArgumentException("Already friends");
            if (f.getStatus() == FriendshipStatus.PENDING) throw new IllegalArgumentException("Request already sent");
        });

        Friendship f = new Friendship();
        f.setRequester(requester);
        f.setAddressee(addressee);
        return toRequestDTO(friendshipRepository.save(f));
    }

    /**
     * Accepts a pending friend request. Only the addressee may call this.
     *
     * @param requestId the friendship record ID
     * @param userId    the ID of the user accepting (must be the addressee)
     * @return the updated friendship DTO with {@code ACCEPTED} status
     * @throws RuntimeException         if the request does not exist
     * @throws IllegalArgumentException if the caller is not the addressee or the request is not PENDING
     */
    @Transactional
    public FriendRequestDTO acceptRequest(Long requestId, Long userId) {
        Friendship f = getRequestForAddressee(requestId, userId);
        f.setStatus(FriendshipStatus.ACCEPTED);
        return toRequestDTO(friendshipRepository.save(f));
    }

    /**
     * Rejects a pending friend request. Only the addressee may call this.
     * The record is kept with {@code REJECTED} status rather than deleted.
     *
     * @param requestId the friendship record ID
     * @param userId    the ID of the user rejecting (must be the addressee)
     * @throws RuntimeException         if the request does not exist
     * @throws IllegalArgumentException if the caller is not the addressee or the request is not PENDING
     */
    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        Friendship f = getRequestForAddressee(requestId, userId);
        f.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(f);
    }

    /**
     * Returns all accepted friends for a user. The friendship record is directional,
     * so the helper resolves which side is the "friend" based on the caller's ID.
     *
     * @param userId the user whose friends to list
     * @return list of friend DTOs with display name, tag, visibility, and friendship ID
     */
    public List<FriendDTO> getFriends(Long userId) {
        return friendshipRepository.findFriends(userId).stream()
                .map(f -> toFriendDTO(f, userId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all pending friend requests received by the user (user is the addressee).
     *
     * @param userId the addressee's ID
     * @return list of pending incoming requests
     */
    public List<FriendRequestDTO> getPendingReceived(Long userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all pending friend requests sent by the user (user is the requester).
     *
     * @param userId the requester's ID
     * @return list of pending outgoing requests
     */
    public List<FriendRequestDTO> getPendingSent(Long userId) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    /**
     * Removes an existing friendship by hard-deleting the record.
     *
     * @param userId   the user initiating the removal
     * @param friendId the friend to remove
     * @throws IllegalArgumentException if no accepted friendship exists between the two users
     */
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        Friendship f = friendshipRepository.findBetweenUsers(userId, friendId)
                .filter(fr -> fr.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new IllegalArgumentException("Amizade não encontrada"));
        friendshipRepository.delete(f);
    }

    /**
     * Searches for users by display name or userTag, excluding the caller.
     * The friendship status with the caller is included in each result for UI rendering.
     *
     * @param myId  the calling user's ID (excluded from results)
     * @param query the search string (case-insensitive substring match)
     * @return list of matching users with their friendship status relative to the caller
     */
    public List<UserSearchResultDTO> searchUsers(Long myId, String query) {
        return userRepository.searchByDisplayNameOrTag(query).stream()
                .filter(u -> !u.getId().equals(myId))
                .map(u -> toSearchResult(u, myId))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether two users have an accepted friendship.
     * Used as an authorization gate by messaging, trade, and sell services.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return {@code true} if an ACCEPTED friendship exists between the two users in either direction
     */
    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.findBetweenUsers(userId1, userId2)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    /**
     * Loads and validates a friendship request for an acceptance/rejection action.
     * Ensures the caller is the addressee and the request is still pending.
     *
     * @param requestId the friendship record ID
     * @param userId    the user attempting to act on the request (must be the addressee)
     * @return the friendship entity ready to be updated
     * @throws RuntimeException         if the request does not exist
     * @throws IllegalArgumentException if the caller is not the addressee or the request is not PENDING
     */
    private Friendship getRequestForAddressee(Long requestId, Long userId) {
        Friendship f = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        if (!f.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Sem permissão");
        }
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Pedido já processado");
        }
        return f;
    }

    /**
     * Resolves which participant is the "friend" from the caller's perspective.
     * Since the model is directional, the friend is whichever side is not the caller.
     */
    private FriendDTO toFriendDTO(Friendship f, Long myId) {
        User friend = f.getRequester().getId().equals(myId) ? f.getAddressee() : f.getRequester();
        FriendDTO dto = new FriendDTO();
        dto.setId(friend.getId());
        dto.setDisplayName(friend.getDisplayName());
        dto.setUserTag(friend.getUserTag());
        dto.setCollectionVisibility(friend.getCollectionVisibility());
        dto.setFriendshipId(f.getId());
        return dto;
    }

    private FriendRequestDTO toRequestDTO(Friendship f) {
        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setId(f.getId());
        dto.setRequesterId(f.getRequester().getId());
        dto.setRequesterDisplayName(f.getRequester().getDisplayName());
        dto.setRequesterUserTag(f.getRequester().getUserTag());
        dto.setAddresseeId(f.getAddressee().getId());
        dto.setAddresseeDisplayName(f.getAddressee().getDisplayName());
        dto.setAddresseeUserTag(f.getAddressee().getUserTag());
        dto.setStatus(f.getStatus());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }

    private UserSearchResultDTO toSearchResult(User u, Long myId) {
        UserSearchResultDTO dto = new UserSearchResultDTO();
        dto.setId(u.getId());
        dto.setDisplayName(u.getDisplayName());
        dto.setUserTag(u.getUserTag());
        friendshipRepository.findBetweenUsers(myId, u.getId())
                .ifPresent(f -> dto.setFriendshipStatus(f.getStatus()));
        return dto;
    }
}
