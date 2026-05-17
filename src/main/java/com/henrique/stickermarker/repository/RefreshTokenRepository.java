package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.RefreshToken;
import com.henrique.stickermarker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
