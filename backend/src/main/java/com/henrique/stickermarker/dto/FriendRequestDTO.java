package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.Data;

import java.time.Instant;

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
