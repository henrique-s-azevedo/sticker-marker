package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Friendship;
import com.henrique.stickermarker.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :uid1 AND f.addressee.id = :uid2) OR (f.requester.id = :uid2 AND f.addressee.id = :uid1)")
    Optional<Friendship> findBetweenUsers(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriends(@Param("userId") Long userId);

    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendshipStatus status);

    long countByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);
}
