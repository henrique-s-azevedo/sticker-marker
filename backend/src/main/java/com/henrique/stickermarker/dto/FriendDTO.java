package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import lombok.Data;

@Data
public class FriendDTO {
    private Long id;
    private String displayName;
    private String userTag;
    private CollectionVisibility collectionVisibility;
    private Long friendshipId;
}
