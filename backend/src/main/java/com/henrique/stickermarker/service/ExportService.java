package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.CollectionExportDTO;
import com.henrique.stickermarker.dto.ExportSection;
import com.henrique.stickermarker.dto.ExportSectionDTO;
import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.CollectionRepository;
import com.henrique.stickermarker.repository.StickerRepository;
import com.henrique.stickermarker.repository.UserDuplicateRepository;
import com.henrique.stickermarker.repository.UserStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a human-readable export of a user's collection status for sharing or printing.
 *
 * <p>The caller selects which sections to include (MISSING, OWNED, DUPLICATES) via
 * {@link ExportSection} flags. The output contains both structured data (section DTOs)
 * and a pre-formatted plain-text representation suitable for copy-paste.</p>
 *
 * <p>Sticker codes are sorted and visually grouped by their 3-character prefix (e.g. "BRA")
 * in the text output, so stickers from the same country/team cluster together.</p>
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    private final CollectionRepository collectionRepository;
    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

    /**
     * Generates the export for the requested sections.
     * Issues two bulk queries (owned and duplicate codes) to avoid per-sticker lookups.
     *
     * @param user         the user whose collection data to export
     * @param collectionId the collection to export
     * @param sections     the set of sections to include in the output
     * @return the export DTO with structured sections and a pre-formatted text block
     * @throws RuntimeException if the collection does not exist
     */
    public CollectionExportDTO generate(User user, Long collectionId, Set<ExportSection> sections) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        Set<String> ownedCodes = userStickerRepository
                .findByUserAndSticker_Collection_Id(user, collectionId)
                .stream()
                .map(us -> us.getSticker().getCode())
                .collect(Collectors.toSet());

        Set<String> duplicateCodes = userDuplicateRepository
                .findByUserAndSticker_Collection_Id(user, collectionId)
                .stream()
                .map(ud -> ud.getSticker().getCode())
                .collect(Collectors.toSet());

        List<String> allCodesSorted = stickerRepository.findByCollection_Id(collectionId)
                .stream()
                .map(Sticker::getCode)
                .sorted()
                .toList();

        List<ExportSectionDTO> exportSections = new ArrayList<>();

        if (sections.contains(ExportSection.MISSING)) {
            List<String> missing = allCodesSorted.stream()
                    .filter(code -> !ownedCodes.contains(code))
                    .toList();
            exportSections.add(buildSection("MISSING STICKERS", missing));
        }
        if (sections.contains(ExportSection.OWNED)) {
            List<String> owned = allCodesSorted.stream()
                    .filter(ownedCodes::contains)
                    .toList();
            exportSections.add(buildSection("OWNED STICKERS", owned));
        }
        if (sections.contains(ExportSection.DUPLICATES)) {
            List<String> duplicates = allCodesSorted.stream()
                    .filter(duplicateCodes::contains)
                    .toList();
            exportSections.add(buildSection("DUPLICATE STICKERS", duplicates));
        }

        CollectionExportDTO dto = new CollectionExportDTO();
        dto.setCollectionName(collection.getName());
        dto.setSections(exportSections);
        dto.setText(buildText(collection.getName(), exportSections));

        return dto;
    }

    private ExportSectionDTO buildSection(String title, List<String> codes) {
        ExportSectionDTO section = new ExportSectionDTO();
        section.setTitle(title);
        section.setCount(codes.size());
        section.setCodes(codes);
        return section;
    }

    /**
     * Formats the export sections into a compact plain-text block.
     * Codes with the same 3-character prefix are placed on the same line separated by spaces.
     * A blank line is inserted whenever the prefix changes, visually separating country/team groups.
     *
     * @param collectionName the album name used as the header
     * @param sections       the pre-built section DTOs
     * @return the formatted plain text
     */
    private String buildText(String collectionName, List<ExportSectionDTO> sections) {
        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(Math.max(collectionName.length(), 20));

        sb.append(collectionName).append("\n");
        sb.append(separator).append("\n");

        for (ExportSectionDTO section : sections) {
            sb.append("\n");
            String header = section.getTitle() + " (" + section.getCount() + ")";
            sb.append(header).append("\n");
            sb.append("-".repeat(header.length())).append("\n");

            List<String> codes = section.getCodes();
            if (codes.isEmpty()) {
                sb.append("(none)\n");
            } else {
                String lastPrefix = null;
                for (int i = 0; i < codes.size(); i++) {
                    String code = codes.get(i);
                    String prefix = code.length() >= 3 ? code.substring(0, 3) : code;
                    if (lastPrefix != null && !prefix.equals(lastPrefix)) {
                        sb.append("\n");
                    } else if (lastPrefix != null) {
                        sb.append("  ");
                    }
                    sb.append(code);
                    lastPrefix = prefix;
                }
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }
}
