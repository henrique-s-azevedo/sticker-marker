package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Response DTO for a user's duplicate sticker record.
 * Includes sticker metadata for display and the current quantity of extras.
 */
@Data
public class UserDuplicateDTO {
    private Long id;
    private String stickerCode;
    private Integer stickerNumber;
    private String teamInitial;
    private String playerName;
    private Integer page;
    private Long collectionId;
    /** Number of extra copies; always ≥ 1 (records with 0 are deleted by the service). */
    private Integer quantity;
}
