package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.CollectionDTO;
import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.service.CollectionService;
import com.henrique.stickermarker.service.StickerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stickers")
public class StickerController {

    private final StickerService stickerService;
    private final CollectionService collectionService;

    public StickerController(StickerService stickerService, CollectionService collectionService) {
        this.stickerService = stickerService;
        this.collectionService = collectionService;
    }

    @PostMapping
    public Sticker create(@RequestBody Sticker sticker) {
        return stickerService.createSticker(sticker);
    }

    @GetMapping
    public List<Sticker> getAll() {
        return stickerService.getAllStickers();
    }

    @GetMapping("/code/{code}")
    public Sticker getByCode(@PathVariable String code) {
        return stickerService.getByCode(code);
    }

    @GetMapping("/collection/{collectionId}")
    public List<Sticker> getByCollection(@PathVariable Long collectionId) {
        CollectionDTO collection = collectionService.getById(collectionId);
        return stickerService.getByCollection(collection);
    }
}
