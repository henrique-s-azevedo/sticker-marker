package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class TradeCalculationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    private List<StickerBriefDTO> myOfferings;
    private List<StickerBriefDTO> friendOfferings;
    private int maxTrades;
}
