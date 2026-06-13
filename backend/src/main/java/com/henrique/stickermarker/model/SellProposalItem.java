package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A single line item within a {@link SellProposal}, representing one sticker at a given price.
 *
 * <p>{@code batchIndex} groups items into price-homogeneous batches within the same proposal.
 * For example, a proposal with two price tiers would have all items of the first tier at
 * {@code batchIndex = 0} and the second tier at {@code batchIndex = 1}.</p>
 *
 * <p>{@code pricePerUnit} uses {@code BigDecimal} with precision 10 and scale 2 to avoid
 * floating-point rounding errors in monetary values.</p>
 *
 * @see SellProposal
 */
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

    /** Code of the sticker being offered. References {@link Sticker#getCode()} without an explicit FK to simplify queries. */
    @Column(nullable = false)
    private String stickerCode;

    /** Price per unit for this sticker. Stored as {@code BigDecimal} for monetary precision. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    /** Batch index this item belongs to, used to group items sharing the same price tier. */
    @Column(nullable = false)
    private Integer batchIndex;
}
