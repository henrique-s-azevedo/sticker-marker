package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Completion statistics for a user's collection.
 * All counts are computed from the user's sticker and duplicate inventories at query time.
 */
@Data
public class CollectionProgressDTO {

    private Long collectionId;
    private String collectionName;

    /** Total stickers defined in the album template. */
    private int total;
    /** Number of distinct sticker codes the user owns (owned, not counting duplicates). */
    private long owned;
    /** Stickers the user is still missing ({@code total - owned}). */
    private int missing;
    /** Total duplicate units across all sticker codes (sum of quantities, not distinct codes). */
    private long duplicates;
    /** Completion percentage: {@code (owned / total) * 100}, rounded to two decimal places. */
    private double percentage;
}
