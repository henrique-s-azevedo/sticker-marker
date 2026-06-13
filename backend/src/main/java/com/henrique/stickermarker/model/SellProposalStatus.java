package com.henrique.stickermarker.model;

/**
 * Lifecycle states of a {@link SellProposal}.
 *
 * <p>A proposal starts as {@code PENDING} and transitions to {@code COMPLETED}
 * when the transaction is finished, or to {@code CANCELLED} if either party
 * cancels before completion.</p>
 */
public enum SellProposalStatus {

    /** Proposal created; awaiting completion of the transaction. */
    PENDING,

    /** Transaction complete — stickers delivered and payment made. Terminal state. */
    COMPLETED,

    /** Proposal cancelled before completion. Terminal state. */
    CANCELLED
}
