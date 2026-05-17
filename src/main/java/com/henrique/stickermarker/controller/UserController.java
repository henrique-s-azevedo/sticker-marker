package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserCreateDTO;
import com.henrique.stickermarker.dto.UserDTO;
import com.henrique.stickermarker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public UserDTO create(@RequestBody @Valid UserCreateDTO dto) {
        return userService.createUser(dto);
    }

    @GetMapping("/me")
    public UserDTO getMe(Authentication authentication) {
        return userService.getUserDTO((Long) authentication.getDetails());
    }
}
