package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import lombok.Data;

/**
 * Friend entry in the user's friend list.
 * Includes the friend's collection visibility so the client can decide
 * whether to offer a link to their public collection without an extra round-trip.
 */
@Data
public class FriendDTO {
    private Long id;
    private String displayName;
    private String userTag;
    private CollectionVisibility collectionVisibility;
    /** ID of the {@code Friendship} record — used to remove or reference the relationship. */
    private Long friendshipId;
}
