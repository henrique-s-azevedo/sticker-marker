package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.service.SellService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/sell")
@RequiredArgsConstructor
public class SellController {

    private final SellService sellService;

    @GetMapping("/calculate-sell/{friendId}")
    public SellCalculationDTO calculateSell(@PathVariable Long friendId, Authentication auth) {
        return sellService.calculateSell(userId(auth), friendId);
    }

    @GetMapping("/calculate-buy/{friendId}")
    public SellCalculationDTO calculateBuy(@PathVariable Long friendId, Authentication auth) {
        return sellService.calculateBuy(userId(auth), friendId);
    }

    @PostMapping("/propose-sell/{friendId}")
    public SellProposalDTO proposeSell(@PathVariable Long friendId, @RequestBody CreateSellDTO dto, Authentication auth) {
        return sellService.proposeSell(userId(auth), friendId, dto);
    }

    @PostMapping("/propose-buy/{friendId}")
    public SellProposalDTO proposeBuy(@PathVariable Long friendId, @RequestBody CreateSellDTO dto, Authentication auth) {
        return sellService.proposeBuy(userId(auth), friendId, dto);
    }

    @PostMapping("/{sellId}/complete")
    public SellProposalDTO completeSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.completeSell(sellId, userId(auth));
    }

    @PostMapping("/{sellId}/cancel")
    public SellProposalDTO cancelSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.cancelSell(sellId, userId(auth));
    }

    @GetMapping("/{sellId}")
    public SellProposalDTO getSell(@PathVariable Long sellId, Authentication auth) {
        return sellService.getSell(sellId, userId(auth));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
