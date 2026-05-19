package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class CollectionStickerStatusDTO {
    private Long id;
    private String code;
    private int number;
    private String playerName;
    private String teamName;
    private String teamInitial;
    private int pageNumber;
    private StickerStatus status;
}
