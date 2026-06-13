package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Legacy login request DTO. Superseded by {@link com.henrique.stickermarker.dto.auth.LoginRequestDTO}
 * — kept for backward compatibility with any existing callers.
 */
@Data
public class UserLoginDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
