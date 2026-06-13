package com.henrique.stickermarker.repository;

import com.henrique.stickermarker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link User} entities.
 *
 * <p>Provides lookups by the three unique user identifiers used in different contexts:
 * {@code email} (for authentication), {@code userTag} (for public searches and invites),
 * and {@code googleId} (for OAuth upserts).</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address. Primary lookup method for local credential authentication.
     *
     * @param email the email address to search for
     * @return the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email is already registered without loading the full entity.
     * Used during registration to fail fast before hashing the password.
     *
     * @param email the email address to check
     * @return {@code true} if an account with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their public tag. Used in invite flows and public profile lookups.
     *
     * @param userTag the public tag to search for
     * @return the user if found
     */
    Optional<User> findByUserTag(String userTag);

    /**
     * Finds a user by their Google OAuth subject identifier.
     * Used during Google login to locate an existing account before creating a new one.
     *
     * @param googleId the Google subject ID from the verified ID token
     * @return the user if found
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Checks whether a given tag is already taken. Used during tag generation to ensure
     * uniqueness before committing a new user account.
     *
     * @param userTag the tag to check
     * @return {@code true} if the tag is already in use
     */
    boolean existsByUserTag(String userTag);

    /**
     * Case-insensitive search for users whose display name or tag contains the query string.
     * Used in the friend-search endpoint {@code /users/search}.
     *
     * @param q the search term (matched as a substring against both fields)
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.userTag) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchByDisplayNameOrTag(@Param("q") String q);
}
