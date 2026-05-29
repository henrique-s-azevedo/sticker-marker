package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVisibilityDTO {
    @NotNull
    private CollectionVisibility visibility;
}
