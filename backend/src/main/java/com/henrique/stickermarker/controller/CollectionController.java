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

/**
 * Controller for sticker collection (album) operations.
 *
 * <p>Endpoints under {@code /collections} serve two purposes:
 * <ul>
 *   <li>Public album catalog: list/get collections and their stickers (no auth required).</li>
 *   <li>User-specific views: sticker status, progress, and export endpoints under
 *       {@code /collections/{id}/me/...} require a valid JWT.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final ExportService exportService;
    private final UserService userService;

    /**
     * Creates a new collection (album template). This is an administrative operation
     * not exposed to regular users in the current frontend.
     *
     * @param dto the collection payload (name, total stickers, total pages)
     * @return the created collection
     */
    @PostMapping
    public CollectionDTO create(@RequestBody CollectionCreateDTO dto) {
        return collectionService.create(dto);
    }

    /**
     * Returns all available collections (album catalog). No authentication required.
     *
     * @return list of all collections
     */
    @GetMapping
    public List<CollectionDTO> getAll() {
        return collectionService.getAll();
    }

    /**
     * Returns a single collection by ID. No authentication required.
     *
     * @param collectionId the ID of the collection to retrieve
     * @return the collection details
     */
    @GetMapping("/{collectionId}")
    public CollectionDTO getById(@PathVariable Long collectionId) {
        return collectionService.getById(collectionId);
    }

    /**
     * Returns all stickers in a collection as a summary list. No authentication required.
     *
     * @param collectionId the target collection
     * @return list of sticker summaries (code, name, page)
     */
    @GetMapping("/{collectionId}/stickers")
    public List<StickerSummaryDTO> getStickers(@PathVariable Long collectionId) {
        return collectionService.getStickersByCollection(collectionId);
    }

    /**
     * Returns all stickers in a collection with the authenticated user's ownership status.
     * Each sticker is annotated with whether the user owns it and how many duplicates they have.
     *
     * @param authentication the security context; userId extracted from {@code authentication.getDetails()}
     * @param collectionId   the target collection
     * @return list of stickers with per-user owned/duplicate status
     */
    @GetMapping("/{collectionId}/me/stickers")
    public List<CollectionStickerStatusDTO> getStickersWithStatus(Authentication authentication, @PathVariable Long collectionId) {
        User user = userService.getById((Long) authentication.getDetails());
        return collectionService.getStickersWithStatus(user, collectionId);
    }

    /**
     * Returns the authenticated user's completion progress for a specific collection.
     *
     * @param authentication the security context; userId extracted from {@code authentication.getDetails()}
     * @param collectionId   the target collection
     * @return progress DTO containing owned count and total sticker count
     */
    @GetMapping("/{collectionId}/me/progress")
    public CollectionProgressDTO getProgress(Authentication authentication, @PathVariable Long collectionId) {
        User user = userService.getById((Long) authentication.getDetails());
        return collectionService.getProgress(user, collectionId);
    }

    /**
     * Generates an export of the authenticated user's collection, optionally filtered by section.
     * If no sections are specified, all sections are included in the export.
     *
     * @param authentication the security context; userId extracted from {@code authentication.getDetails()}
     * @param collectionId   the target collection
     * @param sections       optional set of {@link ExportSection} values to include; defaults to all
     * @return the export DTO containing the requested sections
     */
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
