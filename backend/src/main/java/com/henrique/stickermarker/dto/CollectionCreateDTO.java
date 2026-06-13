package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for creating a new sticker collection (album template).
 * Only used by admin endpoints — regular users cannot create collections.
 */
@Data
public class CollectionCreateDTO {

    /** Human-readable album name (e.g. "FIFA World Cup 2026"). */
    @NotBlank
    private String name;

    /** Total number of stickers in the album, used for progress percentage calculations. */
    @Min(1)
    private int totalStickers;

    /** Total number of pages in the physical album, used for export grouping. */
    @Min(1)
    private int totalPages;
}
