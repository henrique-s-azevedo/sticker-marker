package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class CollectionDTO {
    private Long id;
    private String name;
    private int totalStickers;
    private int totalPages;
}
