package com.henrique.stickermarker.service;

import com.henrique.stickermarker.model.RefreshToken;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the lifecycle of refresh tokens: creation, validation, and deletion.
 *
 * <p>Refresh tokens are stored as UUIDs in the database with a configurable expiry (default 7 days).
 * The token value is opaque to the client — it is not a JWT. Expiry is enforced by comparing
 * {@code expiryDate} to the current instant at validation time rather than relying on the token
 * value itself, so tokens can be revoked server-side by deleting the record.</p>
 *
 * <p>The rotation strategy is: on every full login ({@link AuthService#login} /
 * {@link AuthService#googleLogin}), the old token is deleted and a fresh one is issued.
 * The {@link AuthService#refresh} endpoint does NOT rotate the token — it only issues a
 * new access token while reusing the existing refresh token.</p>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Creates and persists a new refresh token for the given user.
     * Token value is a random UUID; expiry is calculated as {@code now + refreshExpirationDays * 86400s}.
     *
     * @param user the user to create the token for
     * @return the persisted refresh token entity
     */
    public RefreshToken createForUser(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(Instant.now());
        token.setExpiryDate(Instant.now().plusSeconds(refreshExpirationDays * 86_400));
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates a refresh token value and returns the entity if valid.
     * If the token is expired, it is deleted from the database before throwing,
     * so stale tokens do not accumulate.
     *
     * @param tokenValue the raw refresh token string from the client
     * @return the valid refresh token entity
     * @throws IllegalArgumentException if the token does not exist or has expired
     */
    public RefreshToken validateAndGet(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token expired");
        }

        return token;
    }

    /**
     * Deletes all refresh tokens for the given user.
     * Called before issuing a new token on login to invalidate any previous sessions.
     *
     * @param user the user whose tokens should be invalidated
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
