package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for sending a friend request by email address.
 */
@Data
public class AddFriendByEmailDTO {
    @NotBlank
    @Email
    private String email;
}
