package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.TradeProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeProposalRepository extends JpaRepository<TradeProposal, Long> {

    @Query("SELECT t FROM TradeProposal t WHERE t.proposer.id = :userId OR t.counterpart.id = :userId ORDER BY t.updatedAt DESC")
    List<TradeProposal> findByParticipant(@Param("userId") Long userId);
}
