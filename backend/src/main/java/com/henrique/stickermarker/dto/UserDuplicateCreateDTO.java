package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for recording duplicate stickers ({@code POST /me/duplicates}).
 * The user must already own the sticker (have a {@link UserStickerCreateDTO} record)
 * before duplicates can be registered.
 */
@Data
public class UserDuplicateCreateDTO {

    @NotBlank
    private String stickerCode;

    /** Must be at least 1 — zero-quantity duplicates are deleted, not stored. */
    @Min(1)
    private int quantity;
}
