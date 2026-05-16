package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserDuplicateCreateDTO;
import com.henrique.stickermarker.dto.UserDuplicateDTO;
import com.henrique.stickermarker.dto.UserDuplicateUpdateDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserDuplicateService;
import com.henrique.stickermarker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/duplicates")
@RequiredArgsConstructor
public class UserDuplicateController {

    private final UserDuplicateService duplicateService;
    private final UserService userService;

    @PostMapping
    public UserDuplicateDTO createDuplicate(
            @PathVariable Long userId,
            @RequestBody UserDuplicateCreateDTO dto
    ) {
        User user = userService.getById(userId);
        return duplicateService.createDuplicate(user, dto);
    }

    @GetMapping
    public List<UserDuplicateDTO> getDuplicates(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return duplicateService.getUserDuplicates(user);
    }

    @PutMapping("/{stickerCode}")
    public UserDuplicateDTO updateDuplicate(
            @PathVariable Long userId,
            @PathVariable String stickerCode,
            @RequestBody UserDuplicateUpdateDTO dto
    ) {
        User user = userService.getById(userId);
        return duplicateService.updateDuplicate(user, stickerCode, dto);
    }

    @DeleteMapping("/{stickerCode}")
    public void deleteDuplicate(
            @PathVariable Long userId,
            @PathVariable String stickerCode
    ) {
        User user = userService.getById(userId);
        duplicateService.deleteDuplicate(user, stickerCode);
    }
}
