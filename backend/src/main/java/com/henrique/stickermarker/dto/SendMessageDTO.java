package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDTO {
    @NotBlank
    @Size(max = 1000)
    private String content;
}
