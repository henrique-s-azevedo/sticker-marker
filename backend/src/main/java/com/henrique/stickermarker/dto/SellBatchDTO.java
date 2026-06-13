package com.henrique.stickermarker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * A price tier within a sell proposal — a group of stickers sold at the same unit price.
 * A single {@link CreateSellDTO} can contain multiple batches to model tiered pricing
 * (e.g. common stickers at one price, rare stickers at another).
 */
@Data
public class SellBatchDTO {
    private List<String> stickerCodes;
    private BigDecimal pricePerUnit;
}
