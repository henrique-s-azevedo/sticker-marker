package com.henrique.stickermarker.dto;

import lombok.Data;

@Data
public class ConversationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    private MessageDTO lastMessage;
    private long unreadCount;
}
