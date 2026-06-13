package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for bilateral sticker trade proposals between friends.
 *
 * <p>All endpoints require a valid JWT and operate on the authenticated user's trades.
 * The trade lifecycle requires participation from both sides:</p>
 * <ol>
 *   <li>Proposer creates a trade → {@code PENDING_COUNTERPART}</li>
 *   <li>Counterpart responds (accept/reject) → {@code PENDING_PROPOSER} or {@code REJECTED}</li>
 *   <li>Proposer confirms → {@code CONFIRMED}</li>
 *   <li>Either party completes → {@code COMPLETED}</li>
 * </ol>
 */
@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * Calculates the possible sticker exchange between the authenticated user and a friend.
     * Returns stickers each side can offer and wants, based on owned and duplicate inventories.
     * Used to pre-populate the trade proposal form.
     *
     * @param friendId the ID of the friend to calculate a trade with
     * @param auth     the security context
     * @return calculation result with suggested items for both sides
     */
    @GetMapping("/me/trades/calculate/{friendId}")
    public TradeCalculationDTO calculate(@PathVariable Long friendId, Authentication auth) {
        return tradeService.calculate(userId(auth), friendId);
    }

    /**
     * Creates a new trade proposal addressed to a friend.
     * Side effect: generates a system message in the conversation with the counterpart.
     *
     * @param friendId the counterpart's user ID
     * @param dto      the proposal details (sticker codes for both sides, collection ID)
     * @param auth     the security context
     * @return the created trade proposal with {@code PENDING_COUNTERPART} status
     */
    @PostMapping("/me/trades/propose/{friendId}")
    public TradeProposalDTO propose(
            @PathVariable Long friendId,
            @RequestBody @Valid CreateTradeDTO dto,
            Authentication auth) {
        return tradeService.propose(userId(auth), friendId, dto);
    }

    /**
     * Returns all trade proposals where the authenticated user is either proposer or counterpart.
     *
     * @param auth the security context
     * @return list of trade proposals ordered by most recent activity
     */
    @GetMapping("/me/trades")
    public List<TradeProposalDTO> getMyTrades(Authentication auth) {
        return tradeService.getMyTrades(userId(auth));
    }

    /**
     * Returns a specific trade proposal. Only accessible to the proposer or counterpart.
     *
     * @param tradeId the trade proposal ID
     * @param auth    the security context; enforced by the service to be a participant
     * @return the trade proposal details
     */
    @GetMapping("/me/trades/{tradeId}")
    public TradeProposalDTO getTrade(@PathVariable Long tradeId, Authentication auth) {
        return tradeService.getTrade(tradeId, userId(auth));
    }

    /**
     * Counterpart responds to a pending trade proposal (accept or reject).
     * Transitions the trade from {@code PENDING_COUNTERPART} to either
     * {@code PENDING_PROPOSER} (accepted) or {@code REJECTED}.
     * Side effect: generates a system message notifying the proposer.
     *
     * @param tradeId the trade to respond to
     * @param dto     the response (accept: true/false)
     * @param auth    the security context; only the counterpart may call this
     * @return the updated trade proposal
     */
    @PostMapping("/me/trades/{tradeId}/respond")
    public TradeProposalDTO respond(
            @PathVariable Long tradeId,
            @RequestBody RespondTradeDTO dto,
            Authentication auth) {
        return tradeService.respond(tradeId, userId(auth), dto);
    }

    /**
     * Proposer confirms or rejects a trade that the counterpart has accepted.
     * A {@code true} value transitions to {@code CONFIRMED}; {@code false} transitions to {@code REJECTED}.
     * Side effect: generates a system message notifying the counterpart.
     *
     * @param tradeId the trade to confirm
     * @param body    JSON object with boolean {@code "accept"} key
     * @param auth    the security context; only the proposer may call this
     * @return the updated trade proposal
     */
    @PostMapping("/me/trades/{tradeId}/confirm")
    public TradeProposalDTO confirm(
            @PathVariable Long tradeId,
            @RequestBody Map<String, Boolean> body,
            Authentication auth) {
        return tradeService.confirm(tradeId, userId(auth), Boolean.TRUE.equals(body.get("accept")));
    }

    /**
     * Marks a confirmed trade as physically completed.
     * Transitions the trade to {@code COMPLETED}. Either participant may call this.
     *
     * @param tradeId the confirmed trade to mark as done
     * @param auth    the security context
     * @return the updated trade proposal with {@code COMPLETED} status
     */
    @PostMapping("/me/trades/{tradeId}/complete")
    public TradeProposalDTO complete(@PathVariable Long tradeId, Authentication auth) {
        return tradeService.complete(tradeId, userId(auth));
    }

    /** Extracts the authenticated user's ID from the JWT details stored in the security context. */
    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
