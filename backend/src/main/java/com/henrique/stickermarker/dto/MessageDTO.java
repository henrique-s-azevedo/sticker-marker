package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.MessageType;
import com.henrique.stickermarker.model.SellProposalStatus;
import com.henrique.stickermarker.model.TradeStatus;
import lombok.Data;

import java.time.Instant;

/**
 * A single message in a conversation, with optional embedded proposal context.
 * System messages (TRADE_PROPOSAL, SELL_PROPOSAL, BUY_PROPOSAL) carry live proposal
 * status fetched at query time — not the status at message creation — so the chat thread
 * always reflects the current state of the linked proposal.
 */
@Data
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private Instant sentAt;
    /** Non-null once the recipient has read the message. */
    private Instant readAt;
    private MessageType messageType;

    /** ID of the linked trade proposal (null for non-trade messages). */
    private Long tradeProposalId;
    /** Current status of the linked trade proposal (live, not historical). */
    private TradeStatus tradeStatus;

    /** ID of the linked sell proposal (null for non-sell messages). */
    private Long sellProposalId;
    /** Current status of the linked sell proposal (live, not historical). */
    private SellProposalStatus sellProposalStatus;
    /**
     * User ID of the seller in the linked sell proposal.
     * Enables the client to determine which party is buyer vs. seller without fetching the proposal.
     */
    private Long sellProposalSellerId;
}
