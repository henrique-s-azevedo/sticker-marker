package com.henrique.stickermarker.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class InviteCodeResponseDTO {
    private String code;
    private String inviteUrl;
    private Instant expiresAt;
}
