package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserDuplicateUpdateDTO {

    @Min(0)
    private int quantity;
}
