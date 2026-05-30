package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class StickerBriefDTO {
    private String code;
    private int number;
    private String playerName;
    private String teamName;
    private String teamInitial;
}
