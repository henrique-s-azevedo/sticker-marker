package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.CollectionDTO;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.repository.StickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin service layer for sticker catalog access.
 *
 * <p>Stickers are global, immutable catalog entries — they are never user-specific.
 * Most lookups go through {@link #getByCode} because the sticker code (e.g. "BRA1")
 * is the canonical identifier used in trades, duplicates, and exports.</p>
 */
@Service
public class StickerService {

    private final StickerRepository stickerRepository;

    public StickerService(StickerRepository stickerRepository) {
        this.stickerRepository = stickerRepository;
    }

    /**
     * Persists a new sticker catalog entry. Only used during initial data import.
     *
     * @param sticker the sticker entity to save
     * @return the persisted sticker
     */
    public Sticker createSticker(Sticker sticker) {
        return stickerRepository.save(sticker);
    }

    /**
     * Returns all stickers across all collections.
     *
     * @return the complete sticker catalog
     */
    public List<Sticker> getAllStickers() {
        return stickerRepository.findAll();
    }

    /**
     * Looks up a sticker by its canonical code (e.g. "BRA1", "POR10").
     * This is the primary lookup method — code is used as the business key throughout
     * trade proposals, duplicates, and exports.
     *
     * @param code the sticker code
     * @return the sticker entity
     * @throws RuntimeException if no sticker with that code exists (mapped to 404)
     */
    public Sticker getByCode(String code) {
        return stickerRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Sticker not found"));
    }

    /**
     * Returns all stickers belonging to a given collection DTO.
     * Note: this overload accepts a DTO — prefer {@link StickerRepository#findByCollection_Id}
     * when working with collection IDs directly.
     *
     * @param collection the collection DTO
     * @return stickers in that collection
     */
    public List<Sticker> getByCollection(CollectionDTO collection) {
        return stickerRepository.findByCollection(collection);
    }

    /**
     * Looks up a sticker by its numeric database ID.
     *
     * @param stickerId the sticker's primary key
     * @return the sticker entity
     * @throws RuntimeException if no sticker with that ID exists (mapped to 404)
     */
    public Sticker getById(Long stickerId) {
        return stickerRepository.findById(stickerId)
                .orElseThrow(() -> new RuntimeException("Sticker not found"));
    }
}
