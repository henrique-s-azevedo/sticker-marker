package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class UserDuplicateDTO {
    private Long id;
    private String stickerCode;
    private Integer stickerNumber;
    private String teamInitial;
    private String playerName;
    private Integer page;
    private Long collectionId;
    private Integer quantity;
}
