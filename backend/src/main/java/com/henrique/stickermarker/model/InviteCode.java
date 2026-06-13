package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Shareable invite code that allows a user to add friends without knowing their email.
 *
 * <p>Each user has at most one active code at a time. When a new code is generated,
 * the previous one is deactivated by the {@link com.henrique.stickermarker.service.InviteCodeService}.
 * The {@code active} flag acts as a soft-delete to preserve history without affecting
 * lookups for valid codes.</p>
 *
 * <p>Accepting an invite code automatically creates a friend request from the accepting
 * user to the {@code creator} of the code.</p>
 *
 * @see User
 * @see Friendship
 */
@Entity
@Table(name = "invite_codes")
@Data
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Globally unique alphanumeric code that identifies a single user when shared. */
    @Column(unique = true, nullable = false)
    private String code;

    /** User who generated this code. Becomes the target of the friend request when the code is accepted. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private Instant createdAt;

    /** Validity deadline. The service checks this field before processing an acceptance. */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Whether this code is still valid and can be accepted. Set to {@code false} when
     * the user generates a new code or when this code is successfully accepted.
     */
    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
