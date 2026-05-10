package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "user_duplicates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sticker_id"})
)
@Data
public class UserDuplicate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(nullable = false)
    private int quantity;
}
