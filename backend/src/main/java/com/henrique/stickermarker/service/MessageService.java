package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.ConversationDTO;
import com.henrique.stickermarker.dto.MessageDTO;
import com.henrique.stickermarker.dto.SendMessageDTO;
import com.henrique.stickermarker.model.Message;
import com.henrique.stickermarker.model.MessageType;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;

    public MessageDTO send(Long senderId, Long recipientId, SendMessageDTO dto) {
        if (!friendshipService.areFriends(senderId, recipientId)) {
            throw new IllegalArgumentException("Só podes enviar mensagens a amigos");
        }
        return sendInternal(senderId, recipientId, dto.getContent().trim(), MessageType.CHAT, null);
    }

    public MessageDTO sendInternal(Long senderId, Long recipientId, String content, MessageType type, Long tradeProposalId) {
        User sender = userService.getById(senderId);
        User recipient = userService.getById(recipientId);

        Message msg = new Message();
        msg.setSender(sender);
        msg.setRecipient(recipient);
        msg.setContent(content);
        msg.setMessageType(type);
        msg.setTradeProposalId(tradeProposalId);
        return toDTO(messageRepository.save(msg));
    }

    public List<MessageDTO> getConversation(Long myId, Long friendId) {
        if (!friendshipService.areFriends(myId, friendId)) {
            throw new IllegalArgumentException("Não és amigo deste utilizador");
        }
        return messageRepository.findConversation(myId, friendId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markRead(Long myId, Long friendId) {
        messageRepository.markRead(friendId, myId, Instant.now());
    }

    public List<ConversationDTO> getConversations(Long userId) {
        List<Message> all = messageRepository.findByParticipant(userId);

        Map<Long, Message> latestPerPartner = new LinkedHashMap<>();
        for (Message m : all) {
            Long partnerId = m.getSender().getId().equals(userId)
                    ? m.getRecipient().getId()
                    : m.getSender().getId();
            latestPerPartner.putIfAbsent(partnerId, m);
        }

        return latestPerPartner.entrySet().stream().map(entry -> {
            Long partnerId = entry.getKey();
            Message last = entry.getValue();
            User partner = last.getSender().getId().equals(userId)
                    ? last.getRecipient()
                    : last.getSender();

            long unread = all.stream()
                    .filter(m -> m.getSender().getId().equals(partnerId)
                            && m.getRecipient().getId().equals(userId)
                            && m.getReadAt() == null)
                    .count();

            ConversationDTO dto = new ConversationDTO();
            dto.setFriendId(partnerId);
            dto.setFriendDisplayName(partner.getDisplayName());
            dto.setFriendUserTag(partner.getUserTag());
            dto.setLastMessage(toDTO(last));
            dto.setUnreadCount(unread);
            return dto;
        }).collect(Collectors.toList());
    }

    public long countUnread(Long userId) {
        return messageRepository.countUnread(userId);
    }

    private MessageDTO toDTO(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setSenderId(m.getSender().getId());
        dto.setRecipientId(m.getRecipient().getId());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());
        dto.setReadAt(m.getReadAt());
        dto.setMessageType(m.getMessageType() != null ? m.getMessageType() : MessageType.CHAT);
        dto.setTradeProposalId(m.getTradeProposalId());
        return dto;
    }
}
