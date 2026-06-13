package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.ChangePasswordDTO;
import com.henrique.stickermarker.dto.UpdateVisibilityDTO;
import com.henrique.stickermarker.dto.UserCreateDTO;
import com.henrique.stickermarker.dto.UserDTO;
import com.henrique.stickermarker.dto.UserProfileDTO;
import com.henrique.stickermarker.model.EmailVerificationCode;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.EmailVerificationCodeRepository;
import com.henrique.stickermarker.repository.FriendshipRepository;
import com.henrique.stickermarker.repository.UserRepository;
import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Core user management service: account creation, profile retrieval, visibility settings,
 * and the two-step password change flow.
 *
 * <p>Also owns {@link #generateUserTag}, shared by {@link AuthService} for both
 * email/password and Google registrations.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FriendshipRepository friendshipRepository;
    private final EmailVerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    /**
     * Creates a new user account (admin/internal use — not the public registration flow).
     * See {@link AuthService#register} for the public path.
     *
     * @param dto the creation payload (displayName, email, password)
     * @return the persisted user as a DTO
     */
    public UserDTO createUser(UserCreateDTO dto) {
        User user = new User();
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserTag(generateUserTag(dto.getEmail()));
        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    /**
     * Returns a lightweight DTO for the given user ID. Used when the caller only needs
     * public-facing fields (id, displayName, email).
     *
     * @param id the user ID
     * @return the user DTO
     * @throws RuntimeException if no user with that ID exists (mapped to 404)
     */
    public UserDTO getUserDTO(Long id) {
        return toDTO(getById(id));
    }

    /**
     * Loads a full {@link User} entity by ID. Used internally by other services that need
     * the entity rather than a DTO.
     *
     * @param id the user ID
     * @return the user entity
     * @throws RuntimeException if the user does not exist (mapped to 404)
     */
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Returns the authenticated user's full profile, including pending friend-request count
     * and whether the account was linked to Google.
     * The pending count is included so the UI can show a badge without a separate call.
     *
     * @param userId the authenticated user's ID
     * @return the profile DTO
     */
    public UserProfileDTO getProfile(Long userId) {
        User user = getById(userId);
        long pending = friendshipRepository.countByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setUserTag(user.getUserTag());
        dto.setCollectionVisibility(user.getCollectionVisibility());
        dto.setPendingRequestsCount(pending);
        dto.setGoogleAccount(user.getGoogleId() != null);
        return dto;
    }

    /**
     * Updates the collection visibility setting for a user.
     *
     * @param userId the user to update
     * @param dto    the new visibility value
     */
    public void updateVisibility(Long userId, UpdateVisibilityDTO dto) {
        User user = getById(userId);
        user.setCollectionVisibility(dto.getVisibility());
        userRepository.save(user);
    }

    /**
     * Step 1 of the password change flow: generates a 6-digit one-time code, persists it,
     * and sends it to the user's registered email via Brevo.
     * Any previous unused code for this user is deleted first to ensure only one active code
     * exists at a time.
     *
     * @param userId the authenticated user requesting a code
     */
    @Transactional
    public void sendPasswordChangeCode(Long userId) {
        User user = getById(userId);
        verificationCodeRepository.deleteByUserId(userId);
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        EmailVerificationCode vc = new EmailVerificationCode();
        vc.setUserId(userId);
        vc.setCode(code);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        vc.setCreatedAt(LocalDateTime.now());
        vc.setUsed(false);
        verificationCodeRepository.save(vc);
        emailService.sendPasswordChangeCode(user.getEmail(), code);
    }

    /**
     * Step 2 of the password change flow: validates the current password, then checks the
     * verification code (must be unused and not expired) before updating the hash.
     * The code is marked as used on success to prevent replay.
     *
     * @param userId the authenticated user changing their password
     * @param dto    the payload with currentPassword, verificationCode, and newPassword
     * @throws IllegalArgumentException if the current password is wrong, the code is invalid/expired,
     *                                  or the code does not match
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getById(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        EmailVerificationCode vc = verificationCodeRepository
                .findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Verification code is invalid or has expired"));
        if (!vc.getCode().equals(dto.getVerificationCode())) {
            throw new IllegalArgumentException("Verification code is incorrect");
        }
        vc.setUsed(true);
        verificationCodeRepository.save(vc);
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Derives a unique {@code userTag} from an email address.
     * Takes the local part (before {@code @}), strips non-alphanumeric characters,
     * truncates to 20 chars, then appends an incrementing suffix if the base is taken.
     * Falls back to {@code "user"} if the local part is empty after stripping.
     *
     * @param email the email to derive the tag from
     * @return a unique, alphanumeric userTag
     */
    public String generateUserTag(String email) {
        String base = email.split("@")[0]
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) base = "user";
        if (base.length() > 20) base = base.substring(0, 20);

        String tag = base;
        int i = 1;
        while (userRepository.existsByUserTag(tag)) {
            tag = base + i++;
        }
        return tag;
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
