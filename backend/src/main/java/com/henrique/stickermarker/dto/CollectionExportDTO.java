package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

/**
 * Full export result for a user's collection.
 * Contains both structured section data (for programmatic use) and a pre-formatted
 * plain-text representation (for copy-paste sharing).
 */
@Data
public class CollectionExportDTO {
    private String collectionName;
    /** Structured list of sections (MISSING, OWNED, DUPLICATES) in request order. */
    private List<ExportSectionDTO> sections;
    /** Pre-formatted plain text with sections, counts, and prefix-grouped sticker codes. */
    private String text;
}
