package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.dto.CollectionDTO;
import com.henrique.stickermarker.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Sticker} entities.
 *
 * <p>Stickers are global catalog entries within a collection. Lookups by {@code code}
 * are the primary access pattern since sticker codes are the canonical reference
 * throughout the trade, duplicate, and export flows.</p>
 */
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    /**
     * Finds a sticker by its unique code. The primary lookup used across the system
     * wherever a sticker needs to be resolved from its string reference.
     *
     * @param code the sticker code (e.g. {@code "BRA-10"})
     * @return the sticker if found
     */
    Optional<Sticker> findByCode(String code);

    /**
     * Returns all stickers belonging to a collection DTO reference.
     *
     * @param collection the collection DTO to filter by
     * @return list of stickers in the collection
     */
    List<Sticker> findByCollection(CollectionDTO collection);

    /**
     * Returns all stickers in a specific collection by collection ID.
     * Preferred over {@link #findByCollection(CollectionDTO)} when only the ID is available.
     *
     * @param collectionId the collection ID to filter by
     * @return list of stickers in the collection
     */
    List<Sticker> findByCollection_Id(Long collectionId);
}
