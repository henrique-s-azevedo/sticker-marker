package com.henrique.stickermarker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFriendByEmailDTO {
    @NotBlank
    @Email
    private String email;
}
