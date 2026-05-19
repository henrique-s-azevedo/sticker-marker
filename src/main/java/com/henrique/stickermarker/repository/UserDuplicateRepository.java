package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDuplicateRepository extends JpaRepository<UserDuplicate, Long> {

    Optional<UserDuplicate> findByUserAndSticker_Code(User user, String code);

    List<UserDuplicate> findByUser(User user);

    void deleteByUserAndSticker_Code(User user, String code);

    List<UserDuplicate> findByUserAndSticker_Collection_Id(User user, Long collectionId);
}
