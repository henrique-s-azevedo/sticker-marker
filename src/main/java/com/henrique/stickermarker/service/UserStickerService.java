package com.henrique.stickermarker.service;

import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import com.henrique.stickermarker.repository.UserStickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserStickerService {

    private final UserStickerRepository userStickerRepository;

    public UserStickerService(UserStickerRepository userStickerRepository) {
        this.userStickerRepository = userStickerRepository;
    }

    public UserSticker addStickerToUser(User user, Sticker sticker) {

        // Verificar se já existe
        var existing = userStickerRepository.findByUserAndSticker(user, sticker);

        if (existing.isPresent()) {
            throw new RuntimeException("User already has this sticker");
        }

        UserSticker userSticker = new UserSticker();
        userSticker.setUser(user);
        userSticker.setSticker(sticker);

        return userStickerRepository.save(userSticker);
    }

    public List<UserSticker> getUserStickers(User user) {
        return userStickerRepository.findByUser(user);
    }
}
