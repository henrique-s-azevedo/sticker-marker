package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for {@link Message} entities.
 *
 * <p>Messages model both user-typed chat messages and system notifications. Queries
 * are symmetric (sender ↔ recipient) where needed, since a conversation is defined
 * by any two users regardless of message direction.</p>
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves all messages exchanged between two specific users, sorted chronologically.
     * Used to render the chat thread between two friends.
     *
     * @param uid1 ID of one participant
     * @param uid2 ID of the other participant
     * @return all messages between the two users in ascending time order
     */
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :uid1 AND m.recipient.id = :uid2) OR (m.sender.id = :uid2 AND m.recipient.id = :uid1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    /**
     * Retrieves all messages where a user is either sender or recipient, sorted by
     * most recent first. Used to build the conversation list with last-message previews.
     *
     * @param userId the user whose messages are being fetched
     * @return all messages involving the user, newest first
     */
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findByParticipant(@Param("userId") Long userId);

    /**
     * Counts messages received by a user that have not yet been read ({@code readAt IS NULL}).
     * Used for the unread badge on {@code /me/messages/unread}.
     *
     * @param userId the recipient whose unread count is being queried
     * @return total number of unread messages
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.readAt IS NULL")
    long countUnread(@Param("userId") Long userId);

    /**
     * Marks all unread messages from a specific sender to a specific recipient as read.
     * Called when a user opens a conversation to clear the unread indicator.
     *
     * @param senderId    ID of the user whose messages are being marked as read
     * @param recipientId ID of the user who is reading the messages
     * @param now         the timestamp to set as {@code readAt} for affected messages
     * @return number of messages updated
     */
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId AND m.readAt IS NULL")
    int markRead(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId, @Param("now") Instant now);
}
