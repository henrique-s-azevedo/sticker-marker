package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserStickerCreateDTO {

    @NotBlank
    private String stickerCode;
}
