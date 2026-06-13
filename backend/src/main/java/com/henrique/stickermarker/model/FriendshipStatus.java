package com.henrique.stickermarker.model;

/**
 * Possible states of a friendship relationship between two users.
 *
 * <p>The normal lifecycle is: {@code PENDING} → {@code ACCEPTED}.
 * A rejected request remains in the database as {@code REJECTED}
 * for auditing and to prevent immediate re-spamming by the same user.</p>
 *
 * @see Friendship
 */
public enum FriendshipStatus {

    /** Request sent, awaiting a response from the addressee. */
    PENDING,

    /** Request accepted — both users are friends. */
    ACCEPTED,

    /** Request declined by the addressee. The record is kept for auditing purposes. */
    REJECTED
}
