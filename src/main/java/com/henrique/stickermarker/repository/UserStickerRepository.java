package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {

    List<UserSticker> findByUser(User user);

    Optional<UserSticker> findByUserAndSticker_Code(User user, String code);

    void deleteByUserAndSticker_Code(User user, String code);

    long countByUserAndSticker_Collection_Id(User user, Long collectionId);

    List<UserSticker> findByUserAndSticker_Collection_Id(User user, Long collectionId);
}
