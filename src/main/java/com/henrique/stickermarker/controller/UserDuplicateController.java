package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import com.henrique.stickermarker.service.StickerService;
import com.henrique.stickermarker.service.UserDuplicateService;
import com.henrique.stickermarker.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/duplicates")
public class UserDuplicateController {

    private final UserDuplicateService userDuplicateService;
    private final UserService userService;
    private final StickerService stickerService;

    public UserDuplicateController(UserDuplicateService userDuplicateService, UserService userService, StickerService stickerService) {
        this.userDuplicateService = userDuplicateService;
        this.userService = userService;
        this.stickerService = stickerService;
    }

    @PostMapping("/{stickerId}")
    public UserDuplicate addDuplicate(@PathVariable Long userId, @PathVariable Long stickerId) {
        User user = userService.getById(userId);
        Sticker sticker = stickerService.getById(stickerId);
        return userDuplicateService.addDuplicate(user, sticker);
    }

    @GetMapping
    public List<UserDuplicate> getDuplicates(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return userDuplicateService.getUserDuplicates(user);
    }
}
