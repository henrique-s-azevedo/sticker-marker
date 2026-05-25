package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.service.CollectionService;
import com.henrique.stickermarker.service.ExportService;
import com.henrique.stickermarker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final ExportService exportService;
    private final UserService userService;

    @PostMapping
    public CollectionDTO create(@RequestBody CollectionCreateDTO dto) {
        return collectionService.create(dto);
    }

    @GetMapping
    public List<CollectionDTO> getAll() {
        return collectionService.getAll();
    }

    @GetMapping("/{collectionId}")
    public CollectionDTO getById(@PathVariable Long collectionId) {
        return collectionService.getById(collectionId);
    }

    @GetMapping("/{collectionId}/stickers")
    public List<StickerSummaryDTO> getStickers(@PathVariable Long collectionId) {
        return collectionService.getStickersByCollection(collectionId);
    }

    @GetMapping("/{collectionId}/me/stickers")
    public List<CollectionStickerStatusDTO> getStickersWithStatus(Authentication authentication, @PathVariable Long collectionId) {
        User user = userService.getById((Long) authentication.getDetails());
        return collectionService.getStickersWithStatus(user, collectionId);
    }

    @GetMapping("/{collectionId}/me/progress")
    public CollectionProgressDTO getProgress(Authentication authentication, @PathVariable Long collectionId) {
        User user = userService.getById((Long) authentication.getDetails());
        return collectionService.getProgress(user, collectionId);
    }

    @GetMapping("/{collectionId}/me/export")
    public CollectionExportDTO export(
            Authentication authentication,
            @PathVariable Long collectionId,
            @RequestParam(required = false) Set<ExportSection> sections) {
        User user = userService.getById((Long) authentication.getDetails());
        if (sections == null || sections.isEmpty()) {
            sections = Set.of(ExportSection.values());
        }
        return exportService.generate(user, collectionId, sections);
    }
}
