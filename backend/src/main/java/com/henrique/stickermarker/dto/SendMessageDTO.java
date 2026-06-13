package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for sending a chat message to a friend ({@code POST /messages/{friendId}}).
 * Only user-initiated messages use this DTO — system messages (trade/sell notifications)
 * are created programmatically by the service layer.
 */
@Data
public class SendMessageDTO {
    @NotBlank
    @Size(max = 1000)
    private String content;
}
