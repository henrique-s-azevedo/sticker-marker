package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.Data;

/**
 * A user returned by the search endpoint, annotated with the current relationship state.
 * Enables the client to show appropriate actions (add friend, cancel request, view profile)
 * without a separate friendship lookup.
 */
@Data
public class UserSearchResultDTO {
    private Long id;
    private String displayName;
    private String userTag;
    /**
     * Friendship status between the authenticated user and this result.
     * {@code null} when no relationship exists (i.e. the user can send a new request).
     */
    private FriendshipStatus friendshipStatus;
}
