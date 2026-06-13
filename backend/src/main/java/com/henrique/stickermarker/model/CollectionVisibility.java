package com.henrique.stickermarker.model;

/**
 * Defines the visibility levels for a user's sticker collection as seen by other users.
 *
 * <p>The default is {@code FRIENDS_ONLY} — users are private by default and must
 * explicitly opt into broader visibility.</p>
 *
 * <p>Visibility is enforced in
 * {@link com.henrique.stickermarker.controller.PublicCollectionController}
 * before serving another user's collection data.</p>
 */
public enum CollectionVisibility {

    /** Visible to any authenticated user on the platform. */
    PUBLIC,

    /** Visible only to users with an accepted friendship. This is the default. */
    FRIENDS_ONLY,

    /** Visible to the owner only. */
    PRIVATE
}
