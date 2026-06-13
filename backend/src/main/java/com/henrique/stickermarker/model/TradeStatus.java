package com.henrique.stickermarker.model;

/**
 * Lifecycle states of a {@link TradeProposal}.
 *
 * <p>Normal flow for a successful trade:
 * {@code PENDING_COUNTERPART} → {@code PENDING_PROPOSER} → {@code CONFIRMED} → {@code COMPLETED}</p>
 *
 * <p>At any step before {@code COMPLETED}, the trade can transition to {@code REJECTED}
 * if either party refuses.</p>
 */
public enum TradeStatus {

    /** Proposal created; waiting for the counterpart to accept or reject. */
    PENDING_COUNTERPART,

    /** Counterpart accepted; waiting for the proposer's final confirmation before the trade executes. */
    PENDING_PROPOSER,

    /** Proposer confirmed. The trade has been agreed and should be completed physically. */
    CONFIRMED,

    /** Trade refused by either party. Terminal state. */
    REJECTED,

    /** Trade physically completed. Terminal state. */
    COMPLETED
}
