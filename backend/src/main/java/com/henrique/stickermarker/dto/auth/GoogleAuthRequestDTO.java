package com.henrique.stickermarker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the Google Sign-In endpoint ({@code POST /auth/google}).
 * The frontend performs the Google Sign-In and sends only the resulting ID token here;
 * the backend verifies it against Google's public endpoint.
 */
@Data
public class GoogleAuthRequestDTO {
    /** The raw Google ID token obtained from the frontend's Google Sign-In SDK. */
    @NotBlank
    private String idToken;
}
