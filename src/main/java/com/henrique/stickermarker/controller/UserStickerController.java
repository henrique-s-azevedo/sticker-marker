package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import com.henrique.stickermarker.service.StickerService;
import com.henrique.stickermarker.service.UserService;
import com.henrique.stickermarker.service.UserStickerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/stickers")
public class UserStickerController {

    private final UserStickerService userStickerService;
    private final UserService userService;
    private final StickerService stickerService;

    public UserStickerController(UserStickerService userStickerService, UserService userService, StickerService stickerService) {
        this.userStickerService = userStickerService;
        this.userService = userService;
        this.stickerService = stickerService;
    }

    @PostMapping("/{stickerId}")
    public UserSticker addSticker(@PathVariable Long userId, @PathVariable Long stickerId) {
        User user = userService.getById(userId);
        Sticker sticker = stickerService.getById(stickerId);
        return userStickerService.addStickerToUser(user, sticker);
    }

    @GetMapping
    public List<UserSticker> getUserStickers(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return userStickerService.getUserStickers(user);
    }
}
