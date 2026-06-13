package com.henrique.stickermarker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the token refresh endpoint ({@code POST /auth/refresh}).
 * The refresh token must exist in the database and not have expired.
 */
@Data
public class RefreshTokenRequestDTO {

    @NotBlank
    private String refreshToken;
}
