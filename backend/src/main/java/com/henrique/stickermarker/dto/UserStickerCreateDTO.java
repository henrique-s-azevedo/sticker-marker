package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for adding a sticker to the user's owned collection
 * ({@code POST /me/stickers}).
 */
@Data
public class UserStickerCreateDTO {

    /** The canonical sticker code (e.g. "BRA1") that identifies the sticker in the catalog. */
    @NotBlank
    private String stickerCode;
}
