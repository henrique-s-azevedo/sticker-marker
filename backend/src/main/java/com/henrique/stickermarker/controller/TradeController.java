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

@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/me/trades/calculate/{friendId}")
    public TradeCalculationDTO calculate(@PathVariable Long friendId, Authentication auth) {
        return tradeService.calculate(userId(auth), friendId);
    }

    @PostMapping("/me/trades/propose/{friendId}")
    public TradeProposalDTO propose(
            @PathVariable Long friendId,
            @RequestBody @Valid CreateTradeDTO dto,
            Authentication auth) {
        return tradeService.propose(userId(auth), friendId, dto);
    }

    @GetMapping("/me/trades")
    public List<TradeProposalDTO> getMyTrades(Authentication auth) {
        return tradeService.getMyTrades(userId(auth));
    }

    @GetMapping("/me/trades/{tradeId}")
    public TradeProposalDTO getTrade(@PathVariable Long tradeId, Authentication auth) {
        return tradeService.getTrade(tradeId, userId(auth));
    }

    @PostMapping("/me/trades/{tradeId}/respond")
    public TradeProposalDTO respond(
            @PathVariable Long tradeId,
            @RequestBody RespondTradeDTO dto,
            Authentication auth) {
        return tradeService.respond(tradeId, userId(auth), dto);
    }

    @PostMapping("/me/trades/{tradeId}/confirm")
    public TradeProposalDTO confirm(
            @PathVariable Long tradeId,
            @RequestBody Map<String, Boolean> body,
            Authentication auth) {
        return tradeService.confirm(tradeId, userId(auth), Boolean.TRUE.equals(body.get("accept")));
    }

    @PostMapping("/me/trades/{tradeId}/complete")
    public TradeProposalDTO complete(@PathVariable Long tradeId, Authentication auth) {
        return tradeService.complete(tradeId, userId(auth));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
