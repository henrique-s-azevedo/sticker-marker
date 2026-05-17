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

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createForUser(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(Instant.now());
        token.setExpiryDate(Instant.now().plusSeconds(refreshExpirationDays * 86_400));
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validateAndGet(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token expired");
        }

        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
