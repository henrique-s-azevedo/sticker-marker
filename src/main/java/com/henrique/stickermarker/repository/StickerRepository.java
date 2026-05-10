package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    Sticker findByCode(String code);

    List<Sticker> findByCollection(Collection collection);
}
