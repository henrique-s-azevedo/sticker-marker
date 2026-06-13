package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Association entity that records which stickers a user has marked as owned.
 *
 * <p>The existence of a row indicates the user has claimed the sticker.
 * The {@code hasSticker} flag allows a logical removal (soft-delete) without
 * deleting the record — useful for reverting accidental marks while preserving history.</p>
 *
 * <p>There is no unique database constraint on {@code (user_id, sticker_id)} —
 * the service layer ({@link com.henrique.stickermarker.service.UserStickerService})
 * is responsible for enforcing uniqueness before persisting.</p>
 *
 * @see User
 * @see Sticker
 * @see UserDuplicate
 */
@Entity
@Table(name = "user_stickers")
@Data
public class UserSticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    /**
     * Indicates whether the user actually owns this sticker. Initialized to {@code true}
     * since records are only created when the user marks a sticker as theirs.
     */
    @Column(nullable = false)
    private boolean hasSticker = true;
}
