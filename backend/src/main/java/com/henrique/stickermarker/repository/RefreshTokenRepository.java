package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.RefreshToken;
import com.henrique.stickermarker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link RefreshToken} entities.
 *
 * <p>Supports the JWT token rotation strategy: tokens are looked up by their opaque
 * string value during refresh, and all tokens for a user are deleted on logout to
 * prevent reuse of previously issued refresh tokens.</p>
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its opaque value. Primary lookup during the token
     * refresh flow to validate and rotate an incoming token.
     *
     * @param token the token string submitted by the client
     * @return the token record if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with a user. Called on logout to
     * invalidate all sessions for that user across all devices.
     *
     * @param user the user whose tokens should be revoked
     */
    void deleteByUser(User user);
}
