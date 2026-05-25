package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class CollectionExportDTO {
    private String collectionName;
    private List<ExportSectionDTO> sections;
    private String text;
}
