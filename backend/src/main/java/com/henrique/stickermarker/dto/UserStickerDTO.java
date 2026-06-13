package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Response DTO for a sticker owned by the user (non-duplicate).
 * Includes sticker metadata to avoid a separate catalog lookup on the client side.
 */
@Data
public class UserStickerDTO {
    private Long id;
    private String stickerCode;
    private Integer stickerNumber;
    private Integer pageNumber;
    private String teamInitial;
    private String playerName;
    private Long collectionId;
}
