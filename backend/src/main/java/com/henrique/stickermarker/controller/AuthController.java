package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.auth.AuthResponseDTO;
import com.henrique.stickermarker.dto.auth.GoogleAuthRequestDTO;
import com.henrique.stickermarker.dto.auth.LoginRequestDTO;
import com.henrique.stickermarker.dto.auth.RefreshTokenRequestDTO;
import com.henrique.stickermarker.dto.auth.RegisterRequestDTO;
import com.henrique.stickermarker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody @Valid RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        return authService.refresh(dto);
    }

    @PostMapping("/google")
    public AuthResponseDTO googleLogin(@RequestBody @Valid GoogleAuthRequestDTO dto) {
        return authService.googleLogin(dto.getIdToken());
    }
}
