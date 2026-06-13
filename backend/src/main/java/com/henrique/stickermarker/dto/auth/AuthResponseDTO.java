package com.henrique.stickermarker.dto.auth;

import lombok.Data;

/**
 * Token pair returned by all successful authentication endpoints
 * (register, login, Google login, and token refresh).
 *
 * <p>The access token is a short-lived JWT (15 min) that the client stores in memory.
 * The refresh token is a long-lived opaque UUID (7 days) stored in {@code localStorage}
 * on the frontend and in the {@code refresh_tokens} database table on the backend.</p>
 */
@Data
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    /** Always {@code "Bearer"} — included for OAuth2-compliant clients. */
    private String tokenType = "Bearer";
}
