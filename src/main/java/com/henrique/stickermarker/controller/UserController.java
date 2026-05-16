package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserCreateDTO;
import com.henrique.stickermarker.dto.UserDTO;
import com.henrique.stickermarker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDTO create(@RequestBody @Valid UserCreateDTO dto) {
        return userService.createUser(dto);
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return userService.getUserDTO(id);
    }
}
