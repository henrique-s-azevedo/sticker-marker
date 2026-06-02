package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUserTag(String userTag);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUserTag(String userTag);

    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.userTag) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchByDisplayNameOrTag(@Param("q") String q);
}
