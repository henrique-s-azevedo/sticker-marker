package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.FriendRequestDTO;
import com.henrique.stickermarker.dto.InviteCodeResponseDTO;
import com.henrique.stickermarker.model.InviteCode;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.InviteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public InviteCodeResponseDTO getOrGenerateCode(Long userId) {
        User creator = userService.getById(userId);

        InviteCode invite = inviteCodeRepository.findByCreatorIdAndActiveTrue(userId)
                .filter(c -> c.getExpiresAt().isAfter(Instant.now()))
                .orElseGet(() -> createNewCode(creator));

        return toDTO(invite);
    }

    public FriendRequestDTO acceptInvite(String code, Long acceptingUserId) {
        InviteCode invite = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (!invite.isActive()) throw new IllegalArgumentException("Código inativo");
        if (invite.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Código expirado");
        if (invite.getCreator().getId().equals(acceptingUserId)) {
            throw new IllegalArgumentException("Não podes aceitar o teu próprio convite");
        }

        return friendshipService.sendRequest(acceptingUserId, invite.getCreator().getId());
    }

    private InviteCode createNewCode(User creator) {
        inviteCodeRepository.findByCreatorIdAndActiveTrue(creator.getId())
                .ifPresent(old -> {
                    old.setActive(false);
                    inviteCodeRepository.save(old);
                });

        InviteCode invite = new InviteCode();
        invite.setCode(UUID.randomUUID().toString().replace("-", ""));
        invite.setCreator(creator);
        invite.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        return inviteCodeRepository.save(invite);
    }

    private InviteCodeResponseDTO toDTO(InviteCode invite) {
        InviteCodeResponseDTO dto = new InviteCodeResponseDTO();
        dto.setCode(invite.getCode());
        dto.setInviteUrl(frontendUrl + "/invite/" + invite.getCode());
        dto.setExpiresAt(invite.getExpiresAt());
        return dto;
    }
}
