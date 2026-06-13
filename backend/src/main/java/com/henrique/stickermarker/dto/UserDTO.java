package com.henrique.stickermarker.dto;

import lombok.Data;

/**
 * Minimal user representation returned by admin endpoints.
 * Does not expose sensitive fields (password hash, Google ID, userTag).
 * Use {@link UserProfileDTO} for the authenticated user's own profile.
 */
@Data
public class UserDTO {

    private Long id;
    private String displayName;
    private String email;
}
