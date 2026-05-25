package com.henrique.stickermarker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name= "stickers")
@Data

public class Sticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private String teamInitial;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = false)
    private  String playerName;

    @Column(nullable = false)
    private int pageNumber;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

}
