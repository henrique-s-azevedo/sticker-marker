package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.auth.AuthResponseDTO;
import com.henrique.stickermarker.dto.auth.GoogleAuthRequestDTO;
import com.henrique.stickermarker.dto.auth.LoginRequestDTO;
import com.henrique.stickermarker.dto.auth.RefreshTokenRequestDTO;
import com.henrique.stickermarker.dto.auth.RegisterRequestDTO;
import com.henrique.stickermarker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for all authentication flows.
 *
 * <p>All endpoints under {@code /auth} are publicly accessible — no JWT is required.
 * Successful responses always return an {@link AuthResponseDTO} containing a fresh
 * access token and refresh token pair.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account with email and password.
     * Side effect: sends a verification email via Brevo. Returns a JWT pair immediately
     * so the user is logged in right after registration without a separate login step.
     *
     * @param dto validated registration payload (email, password, display name)
     * @return access token + refresh token for the newly created account
     */
    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody @Valid RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    /**
     * Authenticates a user with email and password credentials.
     *
     * @param dto validated login payload (email, password)
     * @return access token + refresh token on successful authentication
     */
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid LoginRequestDTO dto) {
        return authService.login(dto);
    }

    /**
     * Issues a new access token using a valid refresh token.
     * Side effect: the submitted refresh token is invalidated and replaced with a new one
     * (token rotation), preventing token reuse after this call.
     *
     * @param dto the current refresh token
     * @return a new access token + new refresh token
     */
    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        return authService.refresh(dto);
    }

    /**
     * Authenticates or registers a user via Google Sign-In.
     * The client must supply a valid Google ID token obtained from the frontend Google SDK.
     * Side effect: if no account exists for the Google subject, a new one is created (upsert).
     *
     * @param dto contains the Google ID token from the client
     * @return access token + refresh token for the authenticated account
     */
    @PostMapping("/google")
    public AuthResponseDTO googleLogin(@RequestBody @Valid GoogleAuthRequestDTO dto) {
        return authService.googleLogin(dto.getIdToken());
    }
}
