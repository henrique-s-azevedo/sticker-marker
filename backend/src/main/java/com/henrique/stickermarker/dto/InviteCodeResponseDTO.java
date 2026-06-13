package com.henrique.stickermarker.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Response for the invite code endpoint ({@code GET /me/invite-code}).
 * Contains both the raw code and a fully resolved shareable URL for convenient display.
 * The code is reusable until expiry — it is not consumed on acceptance.
 */
@Data
public class InviteCodeResponseDTO {
    private String code;
    /** Pre-built URL pointing to the frontend invite-acceptance page. */
    private String inviteUrl;
    private Instant expiresAt;
}
