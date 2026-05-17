package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.UserDuplicateCreateDTO;
import com.henrique.stickermarker.dto.UserDuplicateDTO;
import com.henrique.stickermarker.dto.UserDuplicateUpdateDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.UserDuplicateService;
import com.henrique.stickermarker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/duplicates")
@RequiredArgsConstructor
public class UserDuplicateController {

    private final UserDuplicateService duplicateService;
    private final UserService userService;

    @PostMapping
    public UserDuplicateDTO createDuplicate(Authentication authentication, @RequestBody UserDuplicateCreateDTO dto) {
        return duplicateService.createDuplicate(currentUser(authentication), dto);
    }

    @GetMapping
    public List<UserDuplicateDTO> getDuplicates(Authentication authentication) {
        return duplicateService.getUserDuplicates(currentUser(authentication));
    }

    @PutMapping("/{stickerCode}")
    public UserDuplicateDTO updateDuplicate(
            Authentication authentication,
            @PathVariable String stickerCode,
            @RequestBody UserDuplicateUpdateDTO dto
    ) {
        return duplicateService.updateDuplicate(currentUser(authentication), stickerCode, dto);
    }

    @DeleteMapping("/{stickerCode}")
    public void deleteDuplicate(Authentication authentication, @PathVariable String stickerCode) {
        duplicateService.deleteDuplicate(currentUser(authentication), stickerCode);
    }

    private User currentUser(Authentication authentication) {
        return userService.getById((Long) authentication.getDetails());
    }
}
