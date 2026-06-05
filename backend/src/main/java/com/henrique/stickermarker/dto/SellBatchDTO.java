package com.henrique.stickermarker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SellBatchDTO {
    private List<String> stickerCodes;
    private BigDecimal pricePerUnit;
}
