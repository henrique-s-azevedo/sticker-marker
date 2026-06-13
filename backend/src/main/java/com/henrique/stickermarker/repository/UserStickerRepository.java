package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.model.UserSticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link UserSticker} entities.
 *
 * <p>All methods scope results to a specific user since sticker ownership is always
 * user-specific. Lookups by {@code sticker.code} (rather than {@code sticker.id})
 * align with the project convention of using sticker codes as public references.</p>
 */
public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {

    /**
     * Returns all stickers owned by a specific user across all collections.
     *
     * @param user the user whose stickers are being fetched
     * @return all {@link UserSticker} records for the user
     */
    List<UserSticker> findByUser(User user);

    /**
     * Finds a specific user-sticker record by the sticker's code. Used to check
     * ownership before creating a duplicate entry or when removing a sticker.
     *
     * @param user the owner
     * @param code the sticker code to look up
     * @return the record if the user owns this sticker
     */
    Optional<UserSticker> findByUserAndSticker_Code(User user, String code);

    /**
     * Removes the ownership record for a specific sticker. Used when a user unmarks a sticker.
     *
     * @param user the owner
     * @param code the sticker code to remove
     */
    void deleteByUserAndSticker_Code(User user, String code);

    /**
     * Counts how many stickers a user owns within a specific collection.
     * Used to calculate completion progress for the album.
     *
     * @param user         the user
     * @param collectionId the collection to count stickers for
     * @return the number of owned stickers in that collection
     */
    long countByUserAndSticker_Collection_Id(User user, Long collectionId);

    /**
     * Returns all sticker ownership records for a user within a specific collection.
     * Used when building the sticker status grid for an album view.
     *
     * @param user         the user
     * @param collectionId the collection to filter by
     * @return list of {@link UserSticker} records in that collection
     */
    List<UserSticker> findByUserAndSticker_Collection_Id(User user, Long collectionId);
}
