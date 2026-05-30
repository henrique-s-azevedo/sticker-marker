package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class RespondTradeDTO {
    private boolean accept;
    private List<String> counterpartItems;
}
