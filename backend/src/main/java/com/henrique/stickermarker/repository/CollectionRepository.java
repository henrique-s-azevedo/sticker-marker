package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
}
