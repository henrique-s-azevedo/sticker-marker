package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserStickerCreateDTO;
import com.henrique.stickermarker.dto.UserStickerDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserService;
import com.henrique.stickermarker.service.UserStickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/stickers")
@RequiredArgsConstructor
public class UserStickerController {

    private final UserStickerService userStickerService;
    private final UserService userService;

    @PostMapping
    public UserStickerDTO addSticker(
            @PathVariable Long userId,
            @RequestBody UserStickerCreateDTO dto
    ) {
        User user = userService.getById(userId);
        return userStickerService.addStickerToUser(user, dto);
    }

    @GetMapping
    public List<UserStickerDTO> getUserStickers(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return userStickerService.getUserStickers(user);
    }

    @DeleteMapping("/{stickerCode}")
    public void removeSticker(
            @PathVariable Long userId,
            @PathVariable String stickerCode
    ) {
        User user = userService.getById(userId);
        userStickerService.removeStickerFromUser(user, stickerCode);
    }
}
