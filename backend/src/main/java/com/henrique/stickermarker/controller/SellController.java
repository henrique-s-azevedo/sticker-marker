package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.service.SellService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for sticker sell proposals between friends.
 *
 * <p>All endpoints under {@code /me/sell} require a valid JWT. The sell flow supports
 * two directions: the user can offer their duplicates for sale ({@code propose-sell}) or
 * request to buy from a friend ({@code propose-buy}). Proposals group stickers into
 * price batches defined by the {@link com.henrique.stickermarker.dto.SellBatchDTO}.</p>
 *
 * <p>Sell lifecycle: {@code PENDING} → {@code COMPLETED} or {@code CANCELLED}.</p>
 */
@RestController
@RequestMapping("/me/sell")
@RequiredArgsConstructor
public class SellController {

    private final SellService sellService;

    /**
     * Calculates which of the authenticated user's duplicate stickers a friend might want,
     * based on the friend's missing stickers. Used to pre-populate the sell proposal form.
     *
     * @param friendId the potential buyer's user ID
     * @param auth     the security context
     * @return calculation result with sellable stickers and suggested prices
     */
    @GetMapping("/calculate-sell/{friendId}")
    public SellCalculationDTO calculateSell(@PathVariable Long friendId, Authentication auth) {
        return sellService.calculateSell(userId(auth), friendId);
    }

    /**
     * Calculates which stickers the authenticated user is missing that a friend could sell.
     * The inverse of {@code calculate-sell} — used when the user initiates a buy request.
     *
     * @param friendId the potential seller's user ID
     * @param auth     the security context
     * @return calculation result with buyable stickers and suggested prices
     */
    @GetMapping("/calculate-buy/{friendId}")
    public SellCalculationDTO calculateBuy(@PathVariable Long friendId, Authentication auth) {
        return sellService.calculateBuy(userId(auth), friendId);
    }

    /**
     * Creates a sell proposal from the authenticated user (as seller) to a friend (as buyer).
     * Side effect: generates a system message in the conversation with the buyer.
     *
     * @param friendId the buyer's user ID
     * @param dto      the proposal payload with sticker batches and prices
     * @param auth     the security context
     * @return the created sell proposal with {@code PENDING} status
     */
    @PostMapping("/propose-sell/{friendId}")
    public SellProposalDTO proposeSell(@PathVariable Long friendId, @RequestBody CreateSellDTO dto, Authentication auth) {
        return sellService.proposeSell(userId(auth), friendId, dto);
    }

    /**
     * Creates a buy request from the authenticated user (as buyer) to a friend (as seller).
     * Semantically the same as {@code propose-sell} but with reversed roles.
     * Side effect: generates a system message in the conversation with the seller.
     *
     * @param friendId the seller's user ID
     * @param dto      the proposal payload with sticker batches and prices
     * @param auth     the security context
     * @return the created sell proposal with {@code PENDING} status
     */
    @PostMapping("/propose-buy/{friendId}")
    public SellProposalDTO proposeBuy(@PathVariable Long friendId, @RequestBody CreateSellDTO dto, Authentication auth) {
        return sellService.proposeBuy(userId(auth), friendId, dto);
    }

    /**
     * Marks a pending sell proposal as completed. Either participant may complete it
     * once the physical transaction has occurred.
     *
     * @param sellId the proposal to mark as completed
     * @param auth   the security context; only seller or buyer may complete
     * @return the updated proposal with {@code COMPLETED} status
     */
    @PostMapping("/{sellId}/complete")
    public SellProposalDTO completeSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.completeSell(sellId, userId(auth));
    }

    /**
     * Cancels a pending sell proposal before it is completed.
     *
     * @param sellId the proposal to cancel
     * @param auth   the security context; only seller or buyer may cancel
     * @return the updated proposal with {@code CANCELLED} status
     */
    @PostMapping("/{sellId}/cancel")
    public SellProposalDTO cancelSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.cancelSell(sellId, userId(auth));
    }

    /**
     * Returns a specific sell proposal by ID. Only accessible to the seller or buyer.
     *
     * @param sellId the proposal ID to retrieve
     * @param auth   the security context; enforced by the service to be a participant
     * @return the sell proposal details
     */
    @GetMapping("/{sellId}")
    public SellProposalDTO getSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.getSell(sellId, userId(auth));
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
