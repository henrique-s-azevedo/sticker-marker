package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a bilateral sticker exchange proposal between two users.
 *
 * <p>State flow for a successful trade:
 * {@code PENDING_COUNTERPART} → {@code PENDING_PROPOSER} → {@code CONFIRMED} → {@code COMPLETED}
 * (or {@code REJECTED} at any step before COMPLETED).</p>
 *
 * <p>Trade items are stored as lists of sticker codes via {@code @ElementCollection} —
 * a deliberately simple approach that avoids an intermediate join entity.
 * Item order is preserved by {@code @OrderColumn}.</p>
 *
 * <p>{@code collectionId} constrains the trade to stickers from a single album.
 * Both participants must use stickers from the same collection.</p>
 *
 * @see TradeStatus
 * @see User
 */
@Entity
@Table(name = "trade_proposals")
@Data
public class TradeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who initiated the trade proposal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    /** The user who received the trade proposal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterpart_id", nullable = false)
    private User counterpart;

    /**
     * Sticker codes offered by the {@code proposer} in exchange.
     * Stored in the {@code trade_proposer_items} table, ordered by {@code item_order}.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_proposer_items", joinColumns = @JoinColumn(name = "trade_id"))
    @Column(name = "sticker_code")
    @OrderColumn(name = "item_order")
    private List<String> proposerItems = new ArrayList<>();

    /**
     * Sticker codes the {@code proposer} wants to receive in return.
     * Stored in the {@code trade_counterpart_items} table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_counterpart_items", joinColumns = @JoinColumn(name = "trade_id"))
    @Column(name = "sticker_code")
    @OrderColumn(name = "item_order")
    private List<String> counterpartItems = new ArrayList<>();

    /**
     * The album all stickers in this trade belong to.
     * Initialized to {@code 1L} as a fallback; must always be explicitly set by the service.
     */
    @Column(nullable = false)
    private Long collectionId = 1L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.PENDING_COUNTERPART;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /** Updated automatically on every state change to track when the trade last progressed. */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
