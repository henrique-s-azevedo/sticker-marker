package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserDuplicate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link UserDuplicate} entities.
 *
 * <p>Duplicate stickers are always scoped to a user. Lookups use {@code sticker.code}
 * rather than {@code sticker.id} to match the system-wide convention of sticker codes
 * as the primary external reference.</p>
 */
public interface UserDuplicateRepository extends JpaRepository<UserDuplicate, Long> {

    /**
     * Finds the duplicate record for a specific sticker owned by a user.
     * Used to check existence and retrieve quantity before updating.
     *
     * @param user the owner
     * @param code the sticker code to look up
     * @return the duplicate record if it exists
     */
    Optional<UserDuplicate> findByUserAndSticker_Code(User user, String code);

    /**
     * Returns all duplicate sticker records for a user across all collections.
     *
     * @param user the user whose duplicates are being fetched
     * @return all {@link UserDuplicate} records for the user
     */
    List<UserDuplicate> findByUser(User user);

    /**
     * Removes the duplicate record for a specific sticker. Called when quantity reaches zero
     * rather than keeping a zero-quantity row in the database.
     *
     * @param user the owner
     * @param code the sticker code to remove
     */
    void deleteByUserAndSticker_Code(User user, String code);

    /**
     * Returns all duplicate records for a user within a specific collection.
     * Used when calculating which duplicates are available for trading or selling.
     *
     * @param user         the user
     * @param collectionId the collection to filter by
     * @return list of {@link UserDuplicate} records in that collection
     */
    List<UserDuplicate> findByUserAndSticker_Collection_Id(User user, Long collectionId);
}
