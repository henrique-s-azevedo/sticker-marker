package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Collection} entities.
 *
 * <p>Collections are global album templates shared across all users.
 * Standard CRUD inherited from {@link JpaRepository} is sufficient since
 * collections are typically managed by admins and not queried by complex filters.</p>
 */
public interface CollectionRepository extends JpaRepository<Collection, Long> {
}
