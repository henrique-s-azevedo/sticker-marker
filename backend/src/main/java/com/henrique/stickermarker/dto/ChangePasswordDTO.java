package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for the two-step password change flow ({@code POST /me/change-password}).
 * A verification code must be requested first ({@code POST /me/send-change-password-code})
 * and included here to authorize the change.
 */
@Data
public class ChangePasswordDTO {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    /** One-time code sent to the user's email address by the send-code endpoint. */
    @NotBlank
    private String verificationCode;
}
