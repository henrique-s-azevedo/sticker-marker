package com.henrique.stickermarker.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private Instant sentAt;
    private Instant readAt;
}
