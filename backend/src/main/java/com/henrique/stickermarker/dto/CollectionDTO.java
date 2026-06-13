package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Response DTO for a sticker collection (album template).
 * Returned by catalog endpoints and embedded in other responses that need collection context.
 */
@Data
public class CollectionDTO {
    private Long id;
    private String name;
    private int totalStickers;
    private int totalPages;
}
