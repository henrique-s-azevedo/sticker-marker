package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.ChangePasswordDTO;
import com.henrique.stickermarker.dto.UpdateVisibilityDTO;
import com.henrique.stickermarker.dto.UserProfileDTO;
import com.henrique.stickermarker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me/profile")
    public UserProfileDTO getProfile(Authentication auth) {
        return userService.getProfile(userId(auth));
    }

    @PutMapping("/me/collection/visibility")
    public ResponseEntity<Void> updateVisibility(
            @RequestBody @Valid UpdateVisibilityDTO dto,
            Authentication auth) {
        userService.updateVisibility(userId(auth), dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordDTO dto,
            Authentication auth) {
        userService.changePassword(userId(auth), dto);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
