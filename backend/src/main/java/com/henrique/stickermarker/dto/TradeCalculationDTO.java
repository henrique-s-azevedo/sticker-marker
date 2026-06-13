package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * Pre-proposal trade analysis between the authenticated user and one friend.
 * Shows which stickers each party can offer (user's duplicates that the friend is missing,
 * and vice versa) and caps the trade size at {@code maxTrades}.
 *
 * <p>Used by the frontend to populate the trade proposal form without manual sticker selection.</p>
 */
@Data
public class TradeCalculationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    /** Stickers the authenticated user can give: their duplicates that the friend is missing. */
    private List<StickerBriefDTO> myOfferings;
    /** Stickers the friend can give: their duplicates that the authenticated user is missing. */
    private List<StickerBriefDTO> friendOfferings;
    /** The smaller of the two offering list sizes — caps a balanced swap. */
    private int maxTrades;
}
