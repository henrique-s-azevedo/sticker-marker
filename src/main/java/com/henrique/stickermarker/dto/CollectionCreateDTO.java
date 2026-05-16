package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollectionCreateDTO {

    @NotBlank
    private String name;

    @Min(1)
    private int totalStickers;

    @Min(1)
    private int totalPages;
}
