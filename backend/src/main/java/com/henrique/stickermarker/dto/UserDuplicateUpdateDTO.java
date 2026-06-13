package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Request body for updating a duplicate sticker quantity ({@code PUT /me/duplicates/{code}}).
 * Setting quantity to 0 deletes the duplicate record rather than storing a zero entry.
 */
@Data
public class UserDuplicateUpdateDTO {

    /** 0 deletes the record; any positive value updates the stored quantity. */
    @Min(0)
    private int quantity;
}
