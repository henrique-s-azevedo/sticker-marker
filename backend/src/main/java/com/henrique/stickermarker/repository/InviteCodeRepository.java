package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link InviteCode} entities.
 */
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    /**
     * Finds an invite code by its string value. Used when a user submits a code to
     * accept a friend invite — the code is resolved to its creator before creating the friendship.
     *
     * @param code the invite code string
     * @return the code record if found
     */
    Optional<InviteCode> findByCode(String code);

    /**
     * Finds the currently active invite code for a specific user.
     * Each user has at most one active code; this method is used both to retrieve
     * the code for display and to check whether a new one needs to be generated.
     *
     * @param creatorId the ID of the user who owns the code
     * @return the active invite code if one exists
     */
    Optional<InviteCode> findByCreatorIdAndActiveTrue(Long creatorId);
}
