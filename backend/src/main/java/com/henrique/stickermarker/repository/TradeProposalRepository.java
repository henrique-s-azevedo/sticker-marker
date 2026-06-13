package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.TradeProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for {@link TradeProposal} entities.
 */
public interface TradeProposalRepository extends JpaRepository<TradeProposal, Long> {

    /**
     * Returns all trade proposals involving a specific user as either proposer or counterpart,
     * sorted by most recently updated first. Ensures both sides of the trade see the same list.
     *
     * @param userId the user whose proposals are being fetched
     * @return list of trade proposals involving the user, newest activity first
     */
    @Query("SELECT t FROM TradeProposal t WHERE t.proposer.id = :userId OR t.counterpart.id = :userId ORDER BY t.updatedAt DESC")
    List<TradeProposal> findByParticipant(@Param("userId") Long userId);
}
