package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.auth.AuthResponseDTO;
import com.henrique.stickermarker.dto.auth.LoginRequestDTO;
import com.henrique.stickermarker.dto.auth.RefreshTokenRequestDTO;
import com.henrique.stickermarker.dto.auth.RegisterRequestDTO;
import com.henrique.stickermarker.model.RefreshToken;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.UserRepository;
import com.henrique.stickermarker.security.JwtUtil;
import com.henrique.stickermarker.service.GoogleTokenVerifier.GoogleTokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Handles all authentication flows: email/password registration and login,
 * Google ID token login, and access token refresh.
 *
 * <p>Every successful authentication issues a short-lived JWT access token (15 min)
 * and a long-lived refresh token (7 days). The refresh token is stored in the database
 * and replaced on every {@link #login} or {@link #googleLogin} call to invalidate
 * any previously issued token for the same user.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final GoogleTokenVerifier googleTokenVerifier;

    /**
     * Registers a new user with email and password.
     * Generates a unique {@code userTag} derived from the email local part.
     * The account is active immediately — no email verification is required.
     *
     * @param dto the registration payload (displayName, email, password)
     * @return a fresh access/refresh token pair
     * @throws IllegalArgumentException if the email is already registered
     */
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserTag(userService.generateUserTag(dto.getEmail()));
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Authenticates an existing user with email and password via Spring's
     * {@link AuthenticationManager}, which delegates to {@link CustomUserDetailsService}.
     * Side effect: invalidates any existing refresh token for this user before issuing a new one.
     *
     * @param dto the login credentials (email, password)
     * @return a fresh access/refresh token pair
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are wrong
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();

        // Replace any existing refresh token
        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    /**
     * Authenticates or registers a user via a Google ID token (not the OAuth code flow).
     * The token is verified against Google's public endpoint; the {@code aud} claim is checked
     * against the configured client ID to prevent token injection from other apps.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Find by {@code googleId} (returning user).</li>
     *   <li>Find by email and link the Google ID (existing email/password user switching to Google).</li>
     *   <li>Create a new account (first-time Google sign-in).</li>
     * </ol>
     * New accounts receive a random placeholder {@code passwordHash} prefixed with {@code "GOOGLE_"}
     * so they cannot authenticate via the password flow.</p>
     *
     * <p>Side effect: invalidates any existing refresh token before issuing a new one.</p>
     *
     * @param idToken the Google ID token from the frontend
     * @return a fresh access/refresh token pair
     * @throws IllegalArgumentException if the token is invalid or the audience does not match
     */
    public AuthResponseDTO googleLogin(String idToken) {
        GoogleTokenInfo info = googleTokenVerifier.verify(idToken);

        User user = userRepository.findByGoogleId(info.sub())
                .orElseGet(() -> userRepository.findByEmail(info.email())
                        .map(existing -> {
                            existing.setGoogleId(info.sub());
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail(info.email());
                            newUser.setDisplayName(info.name() != null ? info.name() : info.email());
                            newUser.setGoogleId(info.sub());
                            newUser.setPasswordHash("GOOGLE_" + UUID.randomUUID());
                            newUser.setUserTag(userService.generateUserTag(info.email()));
                            return userRepository.save(newUser);
                        }));

        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    /**
     * Issues a new access token for an existing, non-expired refresh token.
     * The refresh token itself is NOT rotated here — rotation happens only on full login.
     *
     * @param dto the refresh token value
     * @return a new access token paired with the same refresh token
     * @throws IllegalArgumentException if the refresh token is invalid or expired
     */
    public AuthResponseDTO refresh(RefreshTokenRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenService.validateAndGet(dto.getRefreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(dto.getRefreshToken());
        return response;
    }

    /**
     * Builds the auth response by generating both tokens for the given user.
     * All authentication paths converge here to guarantee a consistent token shape.
     *
     * @param user the authenticated user
     * @return DTO containing the access token and the persisted refresh token value
     */
    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createForUser(user);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken.getToken());
        return response;
    }
}
