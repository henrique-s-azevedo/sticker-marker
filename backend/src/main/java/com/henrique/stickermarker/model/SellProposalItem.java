package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "sell_proposal_items")
@Data
public class SellProposalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_proposal_id", nullable = false)
    private SellProposal sellProposal;

    @Column(nullable = false)
    private String stickerCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private Integer batchIndex;
}
