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

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/me/messages/unread")
    public Map<String, Long> getUnreadCount(Authentication auth) {
        return Map.of("count", messageService.countUnread(userId(auth)));
    }

    @PostMapping("/me/messages/{friendId}")
    public MessageDTO send(
            @PathVariable Long friendId,
            @RequestBody @Valid SendMessageDTO dto,
            Authentication auth) {
        return messageService.send(userId(auth), friendId, dto);
    }

    @GetMapping("/me/messages/{friendId}")
    public List<MessageDTO> getConversation(@PathVariable Long friendId, Authentication auth) {
        return messageService.getConversation(userId(auth), friendId);
    }

    @GetMapping("/me/conversations")
    public List<ConversationDTO> getConversations(Authentication auth) {
        return messageService.getConversations(userId(auth));
    }

    @PostMapping("/me/conversations/{friendId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long friendId, Authentication auth) {
        messageService.markRead(userId(auth), friendId);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
