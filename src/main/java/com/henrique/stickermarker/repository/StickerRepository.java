package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.dto.CollectionDTO;
import com.henrique.stickermarker.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StickerRepository extends JpaRepository<Sticker, Long> {

    Optional<Sticker> findByCode(String code);

    List<Sticker> findByCollection(CollectionDTO collection);

    List<Sticker> findByCollection_Id(Long collectionId);
}
