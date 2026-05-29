package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.Data;

@Data
public class UserSearchResultDTO {
    private Long id;
    private String displayName;
    private String userTag;
    private FriendshipStatus friendshipStatus;
}
