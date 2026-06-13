package com.henrique.stickermarker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for the public registration endpoint ({@code POST /auth/register}).
 * The user tag is auto-generated from the email; the account is active immediately.
 */
@Data
public class RegisterRequestDTO {

    @NotBlank
    private String displayName;

    @Email
    @NotBlank
    private String email;

    /** Minimum 8 characters — stricter than the admin creation path. */
    @NotBlank
    @Size(min = 8)
    private String password;
}
