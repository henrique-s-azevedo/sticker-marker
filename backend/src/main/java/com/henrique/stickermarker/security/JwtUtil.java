package com.henrique.stickermarker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for JWT operations: token generation, validation, and claim extraction.
 *
 * <p>Tokens are signed with HMAC-SHA256 using the secret configured in
 * {@code app.jwt.secret}. The {@code userId} is embedded as a custom claim so
 * downstream filters can extract it without a database lookup.</p>
 *
 * <p>Access token lifetime is controlled by {@code app.jwt.expiration-ms} (default 15 minutes).
 * Refresh token lifecycle is managed separately by
 * {@link com.henrique.stickermarker.service.RefreshTokenService}.</p>
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Derives a {@link SecretKey} from the configured secret string on every call.
     * Avoids caching the key at startup to pick up hot-reloaded properties in dev.
     */
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT access token for an authenticated user.
     * Embeds the {@code userId} as a custom claim to allow stateless user identification
     * in {@link JwtAuthenticationFilter} without querying the database on every request.
     *
     * @param userId the user's database ID, embedded as claim {@code "userId"}
     * @param email  the user's email, used as the JWT subject
     * @return compact, URL-safe signed JWT string
     */
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Validates a JWT token's signature and expiry without throwing exceptions.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is well-formed, correctly signed, and not expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the user ID from a validated JWT token.
     * Callers must ensure the token is valid before calling this method.
     *
     * @param token a valid, non-expired JWT string
     * @return the {@code userId} claim value
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extracts the email (JWT subject) from a validated JWT token.
     *
     * @param token a valid, non-expired JWT string
     * @return the email address used as the JWT subject
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey()).build()
                .parseSignedClaims(token).getPayload();
    }
}
