package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for {@link EmailVerificationCode} entities.
 *
 * <p>These codes are short-lived and single-use, created for the change-password flow.
 * Lookups always filter by {@code used = false} and {@code expiresAt} to ensure only
 * valid, unexpired, and unconsumed codes can be accepted.</p>
 */
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    /**
     * Finds the most recently created, unused, and non-expired verification code for a user.
     * The {@code Top} qualifier ensures only one result is returned even if multiple valid
     * codes exist — the latest is always preferred.
     *
     * @param userId the user whose code is being looked up
     * @param now    the current time; codes expiring before this value are excluded
     * @return the most recent valid code for the user, if one exists
     */
    Optional<EmailVerificationCode> findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    /**
     * Deletes all verification codes for a user. Called after successful password change
     * to clean up any remaining unused codes and prevent reuse.
     *
     * @param userId the user whose codes should be removed
     */
    void deleteByUserId(Long userId);
}
