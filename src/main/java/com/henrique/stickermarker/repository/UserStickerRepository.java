package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {
    // List of Stickers owned by a user
    List<UserSticker> findByUser(User user);

    // Verify if user already has a specific sticker
    Optional<UserSticker> findByUserAndSticker(User user, Sticker sticker);
}
