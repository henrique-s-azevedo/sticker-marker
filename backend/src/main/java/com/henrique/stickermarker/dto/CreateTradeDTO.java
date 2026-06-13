package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request body for creating a trade proposal ({@code POST /trades/propose/{friendId}}).
 * Both sticker lists must be non-empty — one-sided transfers are not supported by the trade flow
 * (see the sell flow instead).
 */
@Data
public class CreateTradeDTO {
    /** Sticker codes the proposer offers to give. */
    @NotEmpty
    private List<String> proposerItems;
    /** Sticker codes the proposer wants to receive from the counterpart. */
    @NotEmpty
    private List<String> counterpartItems;
}
