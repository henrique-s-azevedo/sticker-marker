package com.henrique.stickermarker.dto.auth;

import lombok.Data;

@Data
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
