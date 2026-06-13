package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Friendship;
import com.henrique.stickermarker.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Friendship} entities.
 *
 * <p>Friendship records are directional ({@code requester} → {@code addressee}),
 * so queries that need to work regardless of direction use explicit JPQL with OR clauses.</p>
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Finds a friendship (or pending request) between two users regardless of who initiated it.
     * Used to check whether a relationship already exists before creating a new request.
     *
     * @param uid1 ID of one of the users
     * @param uid2 ID of the other user
     * @return the friendship record if one exists in either direction
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :uid1 AND f.addressee.id = :uid2) OR (f.requester.id = :uid2 AND f.addressee.id = :uid1)")
    Optional<Friendship> findBetweenUsers(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    /**
     * Returns all accepted friendships for a given user, regardless of who sent the request.
     *
     * @param userId the user whose friend list is being fetched
     * @return list of accepted {@link Friendship} records where the user appears on either side
     */
    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriends(@Param("userId") Long userId);

    /**
     * Returns all friendship requests received by a user with a given status.
     * Used to list pending incoming requests for {@code /me/friend-requests}.
     *
     * @param addresseeId the user receiving the requests
     * @param status      the status to filter by (typically {@code PENDING})
     * @return list of matching friendship records
     */
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

    /**
     * Returns all friendship requests sent by a user with a given status.
     * Used to list pending outgoing requests for {@code /me/friend-requests/sent}.
     *
     * @param requesterId the user who sent the requests
     * @param status      the status to filter by (typically {@code PENDING})
     * @return list of matching friendship records
     */
    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendshipStatus status);

    /**
     * Counts pending friend requests received by a user.
     * Used by the notification badge on {@code /me/friend-requests/count}.
     *
     * @param addresseeId the user receiving the requests
     * @param status      the status to count (typically {@code PENDING})
     * @return number of matching records
     */
    long countByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);
}
