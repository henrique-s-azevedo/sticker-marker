package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDuplicateCreateDTO {

    @NotBlank
    private String stickerCode;

    @Min(1)
    private int quantity;
}
