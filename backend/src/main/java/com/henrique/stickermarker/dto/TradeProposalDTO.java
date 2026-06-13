package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.TradeStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Full trade proposal response, returned after creation and by proposal-lookup endpoints.
 * Includes participant details and the sticker lists for both sides of the exchange.
 *
 * <p>State machine: {@code PENDING_COUNTERPART → PENDING_PROPOSER → CONFIRMED → COMPLETED},
 * with {@code REJECTED} as a terminal failure state.</p>
 */
@Data
public class TradeProposalDTO {
    private Long id;
    private Long proposerId;
    private String proposerName;
    private String proposerUserTag;
    private Long counterpartId;
    private String counterpartName;
    private String counterpartUserTag;
    private List<StickerBriefDTO> proposerItems;
    private List<StickerBriefDTO> counterpartItems;
    private TradeStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
