package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * A sticker annotated with its ownership status for a specific user.
 * Returned by the sticker-with-status endpoint and used internally by trade/sell
 * calculation logic to determine which stickers can be offered or requested.
 *
 * <p>{@code duplicateQuantity} is only meaningful when {@code status == DUPLICATE};
 * it is 0 for OWNED and MISSING stickers.</p>
 */
@Data
public class CollectionStickerStatusDTO {
    private Long id;
    /** Canonical sticker code — the business key used in all trade and sell operations. */
    private String code;
    private int number;
    private String playerName;
    private String teamName;
    private String teamInitial;
    private int pageNumber;
    private StickerStatus status;
    /** Number of extra copies registered (0 unless status is DUPLICATE). */
    private int duplicateQuantity;
}
