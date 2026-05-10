package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.UserDuplicate;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDuplicateRepository extends JpaRepository<UserDuplicate, Long> {

    // All duplicates from a user
    List<UserDuplicate> findByUser(User user);

    // All duplicates of a specific sticker
    Optional<UserDuplicate> findByUserAndSticker(User user, Sticker sticker);
}
