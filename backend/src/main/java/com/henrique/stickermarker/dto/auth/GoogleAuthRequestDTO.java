package com.henrique.stickermarker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequestDTO {
    @NotBlank
    private String idToken;
}
