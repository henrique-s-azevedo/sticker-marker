package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.ChangePasswordDTO;
import com.henrique.stickermarker.dto.UpdateVisibilityDTO;
import com.henrique.stickermarker.dto.UserCreateDTO;
import com.henrique.stickermarker.dto.UserDTO;
import com.henrique.stickermarker.dto.UserProfileDTO;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.FriendshipRepository;
import com.henrique.stickermarker.repository.UserRepository;
import com.henrique.stickermarker.model.FriendshipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FriendshipRepository friendshipRepository;

    public UserDTO createUser(UserCreateDTO dto) {
        User user = new User();
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserTag(generateUserTag(dto.getEmail()));
        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    public UserDTO getUserDTO(Long id) {
        return toDTO(getById(id));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

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
        return dto;
    }

    public void updateVisibility(Long userId, UpdateVisibilityDTO dto) {
        User user = getById(userId);
        user.setCollectionVisibility(dto.getVisibility());
        userRepository.save(user);
    }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getById(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Password atual incorreta");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

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

