package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "collections")
@Data
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int totalStickers;

    @Column(nullable = false)
    private int totalPages;
}
