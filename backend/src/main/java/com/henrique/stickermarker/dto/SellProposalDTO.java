package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.SellProposalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class SellProposalDTO {
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerUserTag;
    private Long buyerId;
    private String buyerName;
    private String buyerUserTag;
    private SellProposalStatus status;
    private List<SellBatchDTO> batches;
    private BigDecimal total;
    private Instant createdAt;
}
