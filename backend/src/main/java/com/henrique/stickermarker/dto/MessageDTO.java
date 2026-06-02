package com.henrique.stickermarker.dto;

import com.henrique.stickermarker.model.MessageType;
import com.henrique.stickermarker.model.TradeStatus;
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
    private MessageType messageType;
    private Long tradeProposalId;
    private TradeStatus tradeStatus;
}
