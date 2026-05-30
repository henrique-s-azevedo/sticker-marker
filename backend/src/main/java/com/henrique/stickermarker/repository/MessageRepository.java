package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :uid1 AND m.recipient.id = :uid2) OR (m.sender.id = :uid2 AND m.recipient.id = :uid1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findByParticipant(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.readAt IS NULL")
    long countUnread(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId AND m.readAt IS NULL")
    int markRead(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId, @Param("now") Instant now);
}
