package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_stickers")
@Data

public class UserSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(nullable = false)
    private boolean hasSticker = true;
}
