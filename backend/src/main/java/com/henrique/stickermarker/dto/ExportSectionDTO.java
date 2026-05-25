package com.henrique.stickermarker.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportSectionDTO {
    private String title;
    private int count;
    private List<String> codes;
}
