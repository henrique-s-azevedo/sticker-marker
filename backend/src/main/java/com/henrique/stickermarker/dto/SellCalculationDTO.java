package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class SellCalculationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    private List<StickerBriefDTO> availableStickers;
}
