package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.UserDuplicateCreateDTO;
import com.henrique.stickermarker.dto.UserDuplicateDTO;
import com.henrique.stickermarker.dto.UserDuplicateUpdateDTO;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import com.henrique.stickermarker.repository.UserDuplicateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDuplicateService {

    private final UserDuplicateRepository duplicateRepository;
    private final StickerService stickerService;
    private final UserStickerService userStickerService;

    public UserDuplicateDTO createDuplicate(User user, UserDuplicateCreateDTO dto) {

        String code = dto.getStickerCode();

        var hasSticker = userStickerService
                .getUserStickers(user)
                .stream()
                .anyMatch(s -> s.getStickerCode().equals(code));

        if (!hasSticker) {
            throw new RuntimeException("User must own the sticker before adding duplicates");
        }

        var existing = duplicateRepository.findByUserAndSticker_Code(user, code);
        if (existing.isPresent()) {
            throw new RuntimeException("Duplicate already exists. Use PUT to update quantity.");
        }

        Sticker sticker = stickerService.getByCode(code);

        UserDuplicate duplicate = new UserDuplicate();
        duplicate.setUser(user);
        duplicate.setSticker(sticker);
        duplicate.setQuantity(dto.getQuantity());

        return toDTO(duplicateRepository.save(duplicate));
    }

    public UserDuplicateDTO updateDuplicate(User user, String code, UserDuplicateUpdateDTO dto) {

        var existing = duplicateRepository.findByUserAndSticker_Code(user, code)
                .orElseThrow(() -> new RuntimeException("Duplicate not found"));

        int quantity = dto.getQuantity();

        if (quantity == 0) {
            duplicateRepository.delete(existing);
            return null;
        }

        existing.setQuantity(quantity);
        return toDTO(duplicateRepository.save(existing));
    }

    public List<UserDuplicateDTO> getUserDuplicates(User user) {
        return duplicateRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void deleteDuplicate(User user, String code) {
        duplicateRepository.deleteByUserAndSticker_Code(user, code);
    }

    private UserDuplicateDTO toDTO(UserDuplicate d) {
        UserDuplicateDTO dto = new UserDuplicateDTO();
        dto.setId(d.getId());
        dto.setStickerCode(d.getSticker().getCode());
        dto.setStickerNumber(d.getSticker().getNumber());
        dto.setTeamInitial(d.getSticker().getTeamInitial());
        dto.setPlayerName(d.getSticker().getPlayerName());
        dto.setPage(d.getSticker().getPageNumber());
        dto.setCollectionId(d.getSticker().getCollection().getId());
        dto.setQuantity(d.getQuantity());
        return dto;
    }
}
