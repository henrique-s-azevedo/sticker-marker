package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class CollectionProgressDTO {

    private Long collectionId;
    private String collectionName;

    private int total;
    private long owned;
    private int missing;
    private double percentage;
}
