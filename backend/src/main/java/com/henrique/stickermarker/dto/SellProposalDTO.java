package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.SellProposalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full sell proposal response, returned after creation and by proposal-lookup endpoints.
 * Batches are reconstructed from the stored {@code SellProposalItem} records (grouped by
 * {@code batchIndex}), so the structure mirrors the original {@link CreateSellDTO}.
 */
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
    /** Reconstructed price tiers matching the original batch structure. */
    private List<SellBatchDTO> batches;
    /** Pre-computed total: sum of (pricePerUnit × sticker count) across all batches. */
    private BigDecimal total;
    private Instant createdAt;
}
