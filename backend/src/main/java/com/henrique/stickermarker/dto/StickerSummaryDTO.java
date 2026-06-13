package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Compact sticker representation for catalog browsing and search results.
 * Excludes ownership status — use {@link CollectionStickerStatusDTO} when
 * ownership context is needed.
 */
@Data
public class StickerSummaryDTO {
    private Long id;
    /** Canonical code used as the business key throughout trades, duplicates, and exports. */
    private String code;
    private int number;
    private int page;
    private String teamInitial;
    private String playerName;
}
