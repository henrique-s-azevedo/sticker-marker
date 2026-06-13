package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserCreateDTO;
import com.henrique.stickermarker.dto.UserDTO;
import com.henrique.stickermarker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user account operations.
 *
 * <p>{@code POST /users} is a low-level creation endpoint (no auth required).
 * {@code GET /me} requires a valid JWT and returns the currently authenticated user's profile.</p>
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user account directly. Intended for internal or administrative use;
     * end-user registration should go through {@code POST /auth/register}.
     *
     * @param dto the user creation payload (email, password, display name)
     * @return the created user's public profile
     */
    @PostMapping("/users")
    public UserDTO create(@RequestBody @Valid UserCreateDTO dto) {
        return userService.createUser(dto);
    }

    /**
     * Returns the profile of the currently authenticated user.
     * The user ID is extracted from {@code authentication.getDetails()} as set by
     * {@link com.henrique.stickermarker.security.JwtAuthenticationFilter}.
     *
     * @param authentication the security context populated by the JWT filter
     * @return the authenticated user's public profile
     */
    @GetMapping("/me")
    public UserDTO getMe(Authentication authentication) {
        return userService.getUserDTO((Long) authentication.getDetails());
    }
}
