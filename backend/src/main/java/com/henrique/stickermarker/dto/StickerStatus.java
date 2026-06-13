package com.henrique.stickermarker.dto;

/**
 * Ownership status of a sticker relative to a specific user's collection.
 * Used in {@link CollectionStickerStatusDTO} to annotate each sticker.
 *
 * <p>Priority when mapping: DUPLICATE takes precedence over OWNED.
 * A sticker marked as a duplicate is also implicitly owned.</p>
 */
public enum StickerStatus {
    /** The user owns exactly one copy of this sticker. */
    OWNED,
    /** The user does not have this sticker. */
    MISSING,
    /** The user owns more than one copy and has registered the extras. */
    DUPLICATE
}
