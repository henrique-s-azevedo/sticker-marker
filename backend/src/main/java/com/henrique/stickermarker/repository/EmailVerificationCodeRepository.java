package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    void deleteByUserId(Long userId);
}
