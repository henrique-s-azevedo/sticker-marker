package com.henrique.stickermarker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String displayName;

    @Column(unique = true, nullable = false)
    private String userTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionVisibility collectionVisibility = CollectionVisibility.FRIENDS_ONLY;
}
