package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.UserStickerCreateDTO;
import com.henrique.stickermarker.dto.UserStickerDTO;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import com.henrique.stickermarker.repository.UserStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStickerService {

    private final UserStickerRepository userStickerRepository;
    private final StickerService stickerService;

    public UserStickerDTO addStickerToUser(User user, UserStickerCreateDTO dto) {

        String code = dto.getStickerCode();

        var existing = userStickerRepository.findByUserAndSticker_Code(user, code);
        if (existing.isPresent()) {
            throw new RuntimeException("User already has this sticker");
        }

        Sticker sticker = stickerService.getByCode(code);

        UserSticker userSticker = new UserSticker();
        userSticker.setUser(user);
        userSticker.setSticker(sticker);

        UserSticker saved = userStickerRepository.save(userSticker);

        return toDTO(saved);
    }

    public List<UserStickerDTO> getUserStickers(User user) {
        return userStickerRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void removeStickerFromUser(User user, String stickerCode) {
        userStickerRepository.deleteByUserAndSticker_Code(user, stickerCode);
    }

    private UserStickerDTO toDTO(UserSticker us) {
        UserStickerDTO dto = new UserStickerDTO();
        dto.setId(us.getId());
        dto.setStickerCode(us.getSticker().getCode());
        dto.setStickerNumber(us.getSticker().getNumber());
        dto.setPageNumber(us.getSticker().getPageNumber());
        dto.setTeamInitial(us.getSticker().getTeamInitial());
        dto.setPlayerName(us.getSticker().getPlayerName());
        dto.setCollectionId(us.getSticker().getCollection().getId());
        return dto;
    }
}
