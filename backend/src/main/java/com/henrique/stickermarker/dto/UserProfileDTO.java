package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String displayName;
    private String email;
    private String userTag;
    private CollectionVisibility collectionVisibility;
    private long pendingRequestsCount;
}
