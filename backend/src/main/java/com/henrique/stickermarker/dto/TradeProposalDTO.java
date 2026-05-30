package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.TradeStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

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
