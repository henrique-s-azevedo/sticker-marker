package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a sell offer of duplicate stickers between two users.
 *
 * <p>The {@code seller} offers stickers to the {@code buyer} at prices defined in
 * {@link SellProposalItem}. Items are grouped into batches ({@code batchIndex}) to
 * allow proposals with multiple price tiers within a single transaction.</p>
 *
 * <p>{@code FetchType.EAGER} on {@code seller} and {@code buyer} is intentional:
 * these fields are almost always needed when building the response DTO, avoiding
 * {@code LazyInitializationException} outside of a transaction.</p>
 *
 * <p>{@code CascadeType.ALL} and {@code orphanRemoval = true} on {@code items} means
 * items are fully owned by the proposal — they never exist independently.</p>
 *
 * @see SellProposalItem
 * @see SellProposalStatus
 * @see User
 */
@Entity
@Table(name = "sell_proposals")
@Data
public class SellProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellProposalStatus status = SellProposalStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    /**
     * Items in this proposal, ordered by {@code batchIndex} then {@code id}
     * to ensure consistent rendering of price groups in the frontend.
     */
    @OneToMany(mappedBy = "sellProposal", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("batchIndex ASC, id ASC")
    private List<SellProposalItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /** Updated automatically on every state transition to maintain auditability. */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
