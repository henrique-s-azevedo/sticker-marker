package com.henrique.stickermarker.service;

import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import com.henrique.stickermarker.repository.UserDuplicateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDuplicateService {

    private final UserDuplicateRepository userDuplicateRepository;

    public UserDuplicateService(UserDuplicateRepository userDuplicateRepository) {
        this.userDuplicateRepository = userDuplicateRepository;
    }

    public UserDuplicate addDuplicate(User user, Sticker sticker) {

        var existing = userDuplicateRepository.findByUserAndSticker(user, sticker);

        if (existing.isPresent()) {
            UserDuplicate duplicate = existing.get();
            duplicate.setQuantity(duplicate.getQuantity() + 1);
            return userDuplicateRepository.save(duplicate);
        }

        UserDuplicate newDuplicate = new UserDuplicate();
        newDuplicate.setUser(user);
        newDuplicate.setSticker(sticker);
        newDuplicate.setQuantity(1);

        return userDuplicateRepository.save(newDuplicate);
    }

    public List<UserDuplicate> getUserDuplicates(User user) {
        return userDuplicateRepository.findByUser(user);
    }
}
