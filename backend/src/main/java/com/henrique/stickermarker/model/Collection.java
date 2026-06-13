package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a sticker album template shared across all users.
 *
 * <p>A {@code Collection} is a global catalog entry — it is not user-specific.
 * It defines the album structure (total stickers, total pages) that drives
 * individual user tracking via {@link UserSticker} and {@link UserDuplicate}.</p>
 *
 * @see Sticker
 * @see UserSticker
 * @see UserDuplicate
 */
@Entity
@Table(name = "collections")
@Data
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Total number of unique stickers in the album. Used to calculate each user's completion progress. */
    @Column(nullable = false)
    private int totalStickers;

    /** Total number of physical pages in the album. Used when exporting the collection by section. */
    @Column(nullable = false)
    private int totalPages;
}
