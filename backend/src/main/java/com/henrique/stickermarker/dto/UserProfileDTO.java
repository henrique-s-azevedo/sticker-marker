package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import lombok.Data;

/**
 * Full profile of the authenticated user, returned by {@code GET /me}.
 * Exposes account settings and a pending request badge count so the client
 * can render the header in a single request.
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String displayName;
    private String email;
    private String userTag;
    private CollectionVisibility collectionVisibility;
    /** Number of incoming friend requests awaiting the user's decision — drives the badge. */
    private long pendingRequestsCount;
    /**
     * {@code true} when the account was created via Google Sign-In.
     * Google accounts have no password and cannot use the password-change flow.
     */
    private boolean googleAccount;
}
