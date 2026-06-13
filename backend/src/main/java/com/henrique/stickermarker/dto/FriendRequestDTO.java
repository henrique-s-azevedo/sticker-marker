package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.Data;

import java.time.Instant;

/**
 * Full representation of a friendship request, used for both inbound and outbound views.
 * Returned by pending-request list endpoints so the client can show who sent or received the request.
 */
@Data
public class FriendRequestDTO {
    private Long id;
    private Long requesterId;
    private String requesterDisplayName;
    private String requesterUserTag;
    private Long addresseeId;
    private String addresseeDisplayName;
    private String addresseeUserTag;
    private FriendshipStatus status;
    private Instant createdAt;
}
