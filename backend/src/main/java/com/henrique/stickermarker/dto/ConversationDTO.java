package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Summary of a conversation with one friend, shown in the inbox list.
 * Conversations are implicit (no entity) — derived from the message history between two users.
 * The list is ordered by most-recent message first.
 */
@Data
public class ConversationDTO {
    private Long friendId;
    private String friendDisplayName;
    private String friendUserTag;
    /** The most recent message exchanged, regardless of direction. */
    private MessageDTO lastMessage;
    /** Number of messages FROM the friend that the authenticated user has not yet read. */
    private long unreadCount;
}
