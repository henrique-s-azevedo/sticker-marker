package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * Request body for responding to a trade proposal.
 * The counterpart can accept (optionally overriding their sticker list) or reject.
 *
 * <p>If {@code counterpartItems} is provided, the proposal moves to {@code PENDING_PROPOSER}
 * for the proposer to review the revised offer. If it is {@code null}, the existing counterpart
 * list is kept and the proposal moves to {@code CONFIRMED}.</p>
 */
@Data
public class RespondTradeDTO {
    private boolean accept;
    /**
     * Optional override for the counterpart's sticker list.
     * When non-null, triggers a counter-proposal rather than a direct acceptance.
     */
    private List<String> counterpartItems;
}
