package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Entity representing a friendship relationship (or pending request) between two users.
 *
 * <p>The model is directional: {@code requester} is who initiated the request and
 * {@code addressee} is who received it. This distinction is required to differentiate
 * sent from received requests in the {@code /me/friend-requests/sent} and
 * {@code /me/friend-requests} endpoints.</p>
 *
 * <p>Records persist even after rejection ({@code REJECTED}) to prevent the same user
 * from spamming requests in a loop. Re-send logic is left to the service layer to decide.</p>
 *
 * @see FriendshipStatus
 * @see User
 */
@Entity
@Table(name = "friendships")
@Data
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who initiated the friend request. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** The user who received the friend request. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    /** Set automatically on persist to ensure consistent timestamps regardless of the calling code. */
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
