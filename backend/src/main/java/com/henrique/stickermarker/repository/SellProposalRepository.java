package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.SellProposal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link SellProposal} entities.
 *
 * <p>Standard CRUD inherited from {@link JpaRepository} is sufficient for the current
 * sell flow. Proposal lookups are done by ID; filtering by participant is handled
 * in the service layer after loading proposals by ID.</p>
 */
public interface SellProposalRepository extends JpaRepository<SellProposal, Long> {
}
