package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * One section of a collection export (e.g. "MISSING STICKERS").
 * Sticker codes are sorted and the count is pre-computed for display purposes.
 */
@Data
public class ExportSectionDTO {
    /** Human-readable section title (e.g. "MISSING STICKERS"). */
    private String title;
    /** Number of sticker codes in this section. */
    private int count;
    /** Sorted list of sticker codes belonging to this section. */
    private List<String> codes;
}
