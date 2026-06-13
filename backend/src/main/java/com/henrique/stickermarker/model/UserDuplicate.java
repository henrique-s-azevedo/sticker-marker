package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity tracking the quantity of duplicate stickers a user holds.
 *
 * <p>A duplicate is an extra copy available for trading or selling. The unique
 * constraint on {@code (user_id, sticker_id)} guarantees a single row per
 * user-sticker pair, with the count accumulated in {@code quantity}.</p>
 *
 * <p>Unlike {@link UserSticker} (which tracks base ownership), {@code UserDuplicate}
 * manages the negotiable inventory of spare copies.</p>
 *
 * @see User
 * @see Sticker
 * @see UserSticker
 */
@Entity
@Table(
        name = "user_duplicates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sticker_id"})
)
@Data
public class UserDuplicate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    /**
     * Number of spare copies available for trading or selling.
     * Should never be zero or negative — the record must be removed instead of
     * keeping a zero quantity.
     */
    @Column(nullable = false)
    private int quantity;
}
