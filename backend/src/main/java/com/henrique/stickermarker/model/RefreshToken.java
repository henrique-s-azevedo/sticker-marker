package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Persisted refresh token used in the JWT rotation mechanism.
 *
 * <p>When a client uses a refresh token to obtain a new access token, the current token
 * is invalidated and a new one is issued (token rotation). Persisting tokens in the database
 * enables explicit invalidation on logout and detection of token reuse.</p>
 *
 * <p>The {@code token} column is capped at 500 characters to accommodate UUID-based
 * tokens with sufficient entropy.</p>
 *
 * @see User
 * @see com.henrique.stickermarker.service.RefreshTokenService
 */
@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    /** Opaque token value, randomly generated. Unique per row to guarantee unambiguous lookups. */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /** Expiry date. The service must verify this field before accepting the token. */
    @Column(nullable = false)
    private Instant expiryDate;

    /** Creation timestamp for audit purposes and detection of unusually old tokens. */
    @Column(nullable = false)
    private Instant createdAt;
}
