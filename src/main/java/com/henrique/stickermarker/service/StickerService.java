package com.henrique.stickermarker.service;

import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.repository.StickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StickerService {

    private final StickerRepository stickerRepository;

    public StickerService(StickerRepository stickerRepository) {
        this.stickerRepository = stickerRepository;
    }

    public Sticker createSticker(Sticker sticker) {
        return stickerRepository.save(sticker);
    }

    public List<Sticker> getAllStickers() {
        return stickerRepository.findAll();
    }

    public Sticker getByCode(String code) {
        return stickerRepository.findByCode(code);
    }

    public List<Sticker> getByCollection(Collection collection) {
        return stickerRepository.findByCollection(collection);
    }

    public Sticker getById(Long stickerId) {
        return stickerRepository.findById(stickerId)
                .orElseThrow(() -> new RuntimeException("Sticker not found"));
    }

}
