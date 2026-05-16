package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class UserStickerDTO {
    private Long id;
    private String stickerCode;
    private Integer stickerNumber;
    private Integer pageNumber;
    private String teamInitial;
    private String playerName;
    private Long collectionId;
}