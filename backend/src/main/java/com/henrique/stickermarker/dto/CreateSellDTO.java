package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * Request body for creating a sell proposal ({@code POST /sell/propose/{friendId}}
 * and {@code POST /sell/buy/{friendId}}).
 * Batches group stickers by price tier; each batch is assigned a sequential
 * {@code batchIndex} by the service for later reconstruction.
 */
@Data
public class CreateSellDTO {
    /** One or more price tiers. Each tier lists the sticker codes and a per-unit price. */
    private List<SellBatchDTO> batches;
}
