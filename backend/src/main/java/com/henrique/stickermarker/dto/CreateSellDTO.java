package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateSellDTO {
    private List<SellBatchDTO> batches;
}
