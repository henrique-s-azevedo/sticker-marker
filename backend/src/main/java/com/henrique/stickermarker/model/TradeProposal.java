package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_proposals")
@Data
public class TradeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterpart_id", nullable = false)
    private User counterpart;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_proposer_items", joinColumns = @JoinColumn(name = "trade_id"))
    @Column(name = "sticker_code")
    @OrderColumn(name = "item_order")
    private List<String> proposerItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_counterpart_items", joinColumns = @JoinColumn(name = "trade_id"))
    @Column(name = "sticker_code")
    @OrderColumn(name = "item_order")
    private List<String> counterpartItems = new ArrayList<>();

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

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
