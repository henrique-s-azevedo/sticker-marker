package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateTradeDTO {
    @NotEmpty
    private List<String> proposerItems;
    @NotEmpty
    private List<String> counterpartItems;
}
