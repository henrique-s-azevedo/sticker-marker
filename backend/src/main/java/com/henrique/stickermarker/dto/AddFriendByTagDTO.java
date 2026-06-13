package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for sending a friend request by user tag.
 * The service strips a leading {@code @} if the client includes it.
 */
@Data
public class AddFriendByTagDTO {
    @NotBlank
    private String userTag;
}
