package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * Pre-proposal sell analysis between the authenticated user and one friend.
 * Lists the stickers available for purchase (the seller's duplicates that the buyer is missing),
 * helping the buyer populate the buy-proposal form.
 */
@Data
public class SellCalculationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    /** Stickers the friend has as duplicates that the authenticated user is missing. */
    private List<StickerBriefDTO> availableStickers;
}
