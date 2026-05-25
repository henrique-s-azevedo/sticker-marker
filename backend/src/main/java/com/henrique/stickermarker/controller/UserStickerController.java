package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserStickerCreateDTO;
import com.henrique.stickermarker.dto.UserStickerDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserService;
import com.henrique.stickermarker.service.UserStickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/stickers")
@RequiredArgsConstructor
public class UserStickerController {

    private final UserStickerService userStickerService;
    private final UserService userService;

    @PostMapping
    public UserStickerDTO addSticker(Authentication authentication, @RequestBody UserStickerCreateDTO dto) {
        return userStickerService.addStickerToUser(currentUser(authentication), dto);
    }

    @GetMapping
    public List<UserStickerDTO> getUserStickers(Authentication authentication) {
        return userStickerService.getUserStickers(currentUser(authentication));
    }

    @DeleteMapping("/{stickerCode}")
    public void removeSticker(Authentication authentication, @PathVariable String stickerCode) {
        userStickerService.removeStickerFromUser(currentUser(authentication), stickerCode);
    }

    private User currentUser(Authentication authentication) {
        return userService.getById((Long) authentication.getDetails());
    }
}
