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

@Service
@RequiredArgsConstructor
public class ExportService {

    private final CollectionRepository collectionRepository;
    private final StickerRepository stickerRepository;
    private final UserStickerRepository userStickerRepository;
    private final UserDuplicateRepository userDuplicateRepository;

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
