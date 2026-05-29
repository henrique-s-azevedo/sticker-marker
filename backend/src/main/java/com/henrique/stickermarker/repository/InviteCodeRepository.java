package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    Optional<InviteCode> findByCode(String code);

    Optional<InviteCode> findByCreatorIdAndActiveTrue(Long creatorId);
}
