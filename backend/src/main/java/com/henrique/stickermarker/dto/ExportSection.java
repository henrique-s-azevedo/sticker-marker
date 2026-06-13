package com.henrique.stickermarker.dto;

/**
 * Selects which sticker groups to include in a collection export.
 * Passed as a query parameter set; the caller may request any combination.
 * If none are specified, the controller defaults to all three sections.
 */
public enum ExportSection {
    /** Stickers the user does not yet own. */
    MISSING,
    /** Stickers the user owns (excluding duplicates). */
    OWNED,
    /** Stickers the user owns more than one copy of. */
    DUPLICATES
}
