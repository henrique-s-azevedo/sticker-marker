package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.ConversationDTO;
import com.henrique.stickermarker.dto.MessageDTO;
import com.henrique.stickermarker.dto.SendMessageDTO;
import com.henrique.stickermarker.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for messaging between friends.
 *
 * <p>All endpoints require a valid JWT. Conversations are implicitly defined by the
 * pair of users — there is no explicit conversation entity. System messages generated
 * by the trade/sell flows appear in the same conversation thread as regular chat messages.</p>
 */
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Returns the total number of unread messages for the authenticated user across all conversations.
     * Used to populate the notification badge in the UI.
     *
     * @param auth the security context
     * @return map with key {@code "count"} and the unread message count
     */
    @GetMapping("/me/messages/unread")
    public Map<String, Long> getUnreadCount(Authentication auth) {
        return Map.of("count", messageService.countUnread(userId(auth)));
    }

    /**
     * Sends a message to a friend. The sender must be friends with the recipient;
     * this constraint is enforced by the service layer.
     *
     * @param friendId the ID of the recipient
     * @param dto      the message payload (content)
     * @param auth     the security context
     * @return the persisted message DTO
     */
    @PostMapping("/me/messages/{friendId}")
    public MessageDTO send(
            @PathVariable Long friendId,
            @RequestBody @Valid SendMessageDTO dto,
            Authentication auth) {
        return messageService.send(userId(auth), friendId, dto);
    }

    /**
     * Returns all messages in the conversation between the authenticated user and a specific friend,
     * ordered chronologically. Includes both chat messages and system notifications.
     *
     * @param friendId the ID of the other conversation participant
     * @param auth     the security context
     * @return list of messages in ascending time order
     */
    @GetMapping("/me/messages/{friendId}")
    public List<MessageDTO> getConversation(@PathVariable Long friendId, Authentication auth) {
        return messageService.getConversation(userId(auth), friendId);
    }

    /**
     * Returns a summary of all conversations for the authenticated user, with the last message
     * and unread count per conversation. Used to render the conversation list view.
     *
     * @param auth the security context
     * @return list of conversation summaries ordered by most recent activity
     */
    @GetMapping("/me/conversations")
    public List<ConversationDTO> getConversations(Authentication auth) {
        return messageService.getConversations(userId(auth));
    }

    /**
     * Marks all messages from a specific friend as read.
     * Side effect: sets {@code readAt} on all unread messages from {@code friendId}
     * to the current user.
     *
     * @param friendId the ID of the friend whose messages should be marked as read
     * @param auth     the security context
     * @return 204 No Content on success
     */
    @PostMapping("/me/conversations/{friendId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long friendId, Authentication auth) {
        messageService.markRead(userId(auth), friendId);
        return ResponseEntity.noContent().build();
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
