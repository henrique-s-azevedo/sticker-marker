package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * One-time verification code used in the change-password email flow.
 *
 * <p>Flow: the user requests a code → receives it via email (through Brevo) →
 * submits it alongside the new password. The {@code used} flag ensures each code
 * can only be consumed once, protecting against replay attacks.</p>
 *
 * <p>Note: {@code userId} is stored as a plain {@code Long} rather than a
 * {@code @ManyToOne} relationship to allow lookup without loading the full
 * {@link User} entity.</p>
 *
 * <p>Uses {@code LocalDateTime} (timezone-naive) instead of {@code Instant} —
 * an inconsistency compared to the rest of the model layer. Worth unifying in a
 * future refactor.</p>
 */
@Entity
@Table(name = "email_verification_codes")
@Data
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the user this code belongs to. Not mapped as a FK to avoid unnecessary lazy loading. */
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String code;

    /** Validity deadline. The service rejects codes submitted after this point. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Marked {@code true} after successful use to prevent the same code from being reused. */
    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
