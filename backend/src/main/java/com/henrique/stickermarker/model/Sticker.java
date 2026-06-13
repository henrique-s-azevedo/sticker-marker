package com.henrique.stickermarker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing an individual sticker within a {@link Collection}.
 *
 * <p>{@code code} is the canonical sticker identifier used throughout the system
 * (e.g. {@code "WC-001"}). It is referenced in trades, duplicates, and exports
 * instead of the numeric {@code id}, ensuring readability in cross-user data exchanges.</p>
 *
 * <p>{@code pageNumber} determines which physical album page this sticker belongs to,
 * enabling stickers to be grouped and exported by page.</p>
 *
 * @see Collection
 * @see UserSticker
 * @see UserDuplicate
 */
@Entity
@Table(name = "stickers")
@Data
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private String teamInitial;

    /**
     * Unique, human-readable sticker code (e.g. {@code "BRA-10"}).
     * Used as the reference key in trade proposals, duplicate tracking, and exports.
     */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = false)
    private String playerName;

    /** Album page number where this sticker appears. Used to group stickers by section in exports. */
    @Column(nullable = false)
    private int pageNumber;

    /**
     * {@code @JsonIgnoreProperties} prevents serialization errors when Hibernate
     * returns a lazy proxy for the associated {@link Collection}.
     */
    @ManyToOne(optional = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;
}
