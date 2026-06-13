package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.CollectionVisibility;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for changing the collection visibility setting
 * ({@code PUT /me/collection/visibility}).
 */
@Data
public class UpdateVisibilityDTO {
    /** Must be one of PUBLIC, FRIENDS_ONLY, or PRIVATE. */
    @NotNull
    private CollectionVisibility visibility;
}
