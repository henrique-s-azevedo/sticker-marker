package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.CollectionDTO;
import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.service.CollectionService;
import com.henrique.stickermarker.service.StickerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the global sticker catalog.
 *
 * <p>Stickers are shared entities (not user-specific), so all endpoints are publicly
 * accessible without authentication. These endpoints are primarily used for admin
 * data seeding and catalog browsing.</p>
 */
@RestController
@RequestMapping("/stickers")
public class StickerController {

    private final StickerService stickerService;
    private final CollectionService collectionService;

    public StickerController(StickerService stickerService, CollectionService collectionService) {
        this.stickerService = stickerService;
        this.collectionService = collectionService;
    }

    /**
     * Creates a new sticker in the global catalog. Administrative operation for data seeding.
     *
     * @param sticker the sticker entity to persist (collection must already exist)
     * @return the persisted sticker with its generated ID
     */
    @PostMapping
    public Sticker create(@RequestBody Sticker sticker) {
        return stickerService.createSticker(sticker);
    }

    /**
     * Returns all stickers across all collections. No authentication required.
     *
     * @return complete list of stickers in the catalog
     */
    @GetMapping
    public List<Sticker> getAll() {
        return stickerService.getAllStickers();
    }

    /**
     * Retrieves a sticker by its unique code. Used to resolve a sticker code string
     * to its full details anywhere in the application.
     *
     * @param code the sticker code (e.g. {@code "BRA-10"})
     * @return the sticker with the given code
     */
    @GetMapping("/code/{code}")
    public Sticker getByCode(@PathVariable String code) {
        return stickerService.getByCode(code);
    }

    /**
     * Returns all stickers belonging to a specific collection.
     *
     * @param collectionId the ID of the collection to filter by
     * @return list of stickers in the collection
     */
    @GetMapping("/collection/{collectionId}")
    public List<Sticker> getByCollection(@PathVariable Long collectionId) {
        CollectionDTO collection = collectionService.getById(collectionId);
        return stickerService.getByCollection(collection);
    }
}
