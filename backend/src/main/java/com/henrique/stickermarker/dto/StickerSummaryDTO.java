package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class StickerSummaryDTO {
    private Long id;
    private String code;
    private int number;
    private int page;
    private String teamInitial;
    private String playerName;
}
