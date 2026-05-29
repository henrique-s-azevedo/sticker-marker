package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.FriendRequestDTO;
import com.henrique.stickermarker.dto.InviteCodeResponseDTO;
import com.henrique.stickermarker.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InviteController {

    private final InviteCodeService inviteCodeService;

    @GetMapping("/me/invite")
    public InviteCodeResponseDTO getMyInvite(Authentication auth) {
        return inviteCodeService.getOrGenerateCode(userId(auth));
    }

    @PostMapping("/invite/{code}/accept")
    public FriendRequestDTO acceptInvite(@PathVariable String code, Authentication auth) {
        return inviteCodeService.acceptInvite(code, userId(auth));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getDetails();
    }
}
