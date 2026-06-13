package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a user account on the platform.
 *
 * <p>Supports two mutually compatible authentication modes: local credentials
 * (email + password) and Google OAuth. A Google-authenticated user may have a
 * {@code null} {@code passwordHash} if they never set local credentials.</p>
 *
 * <p>{@code userTag} is the user's public identifier, used for searches, profile
 * sharing, and invites — kept separate from the email address to preserve privacy.</p>
 *
 * @see CollectionVisibility
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * BCrypt hash of the user's password. May be {@code null} for accounts created
     * exclusively via Google OAuth that have never set local credentials.
     */
    @Column(nullable = false)
    private String passwordHash;

    private String displayName;

    /**
     * Unique public identifier for the user (e.g. {@code @john123}).
     * Auto-generated at registration and used in searches and invite flows.
     * Separate from email to avoid exposing the user's email address publicly.
     */
    @Column(unique = true)
    private String userTag;

    /**
     * Identifier provided by Google after OAuth authentication.
     * Null for accounts created with local credentials.
     */
    @Column(unique = true)
    private String googleId;

    /**
     * Controls the visibility of the user's sticker collection to other users.
     * Defaults to {@code FRIENDS_ONLY} to ensure privacy out of the box.
     */
    @Enumerated(EnumType.STRING)
    private CollectionVisibility collectionVisibility = CollectionVisibility.FRIENDS_ONLY;

    /**
     * Ensures backward compatibility for rows created before the {@code collection_visibility}
     * column existed. With {@code ddl-auto=update}, existing rows may have a null value
     * even after the schema migration.
     */
    @PostLoad
    private void postLoad() {
        if (collectionVisibility == null) {
            collectionVisibility = CollectionVisibility.FRIENDS_ONLY;
        }
    }
}
