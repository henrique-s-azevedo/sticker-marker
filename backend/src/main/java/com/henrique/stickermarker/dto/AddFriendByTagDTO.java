package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFriendByTagDTO {
    @NotBlank
    private String userTag;
}
