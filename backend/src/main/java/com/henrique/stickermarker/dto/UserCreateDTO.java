package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for the admin user-creation endpoint ({@code POST /users}).
 * For the public registration flow, see {@link com.henrique.stickermarker.dto.auth.RegisterRequestDTO}.
 */
@Data
public class UserCreateDTO {

    @NotBlank
    private String displayName;

    @Email
    @NotBlank
    private String email;

    /** Minimum 6 characters (admin path uses a shorter minimum than the public registration). */
    @NotBlank
    @Size(min = 6)
    private String password;
}
