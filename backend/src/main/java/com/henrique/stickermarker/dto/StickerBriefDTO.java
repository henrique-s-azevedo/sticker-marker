package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Minimal sticker representation used inside trade and sell proposal responses.
 * Provides just enough detail to render a sticker chip in the UI without
 * requiring a separate catalog lookup.
 */
@Data
public class StickerBriefDTO {
    private String code;
    private int number;
    private String playerName;
    private String teamName;
    private String teamInitial;
}
