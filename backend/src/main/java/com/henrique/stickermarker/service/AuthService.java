package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.auth.AuthResponseDTO;
import com.henrique.stickermarker.dto.auth.LoginRequestDTO;
import com.henrique.stickermarker.dto.auth.RefreshTokenRequestDTO;
import com.henrique.stickermarker.dto.auth.RegisterRequestDTO;
import com.henrique.stickermarker.model.RefreshToken;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.UserRepository;
import com.henrique.stickermarker.security.JwtUtil;
import com.henrique.stickermarker.service.GoogleTokenVerifier.GoogleTokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserTag(userService.generateUserTag(dto.getEmail()));
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();

        // Replace any existing refresh token
        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    public AuthResponseDTO googleLogin(String idToken) {
        GoogleTokenInfo info = googleTokenVerifier.verify(idToken);

        User user = userRepository.findByGoogleId(info.sub())
                .orElseGet(() -> userRepository.findByEmail(info.email())
                        .map(existing -> {
                            existing.setGoogleId(info.sub());
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail(info.email());
                            newUser.setDisplayName(info.name() != null ? info.name() : info.email());
                            newUser.setGoogleId(info.sub());
                            newUser.setPasswordHash("GOOGLE_" + UUID.randomUUID());
                            newUser.setUserTag(userService.generateUserTag(info.email()));
                            return userRepository.save(newUser);
                        }));

        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    public AuthResponseDTO refresh(RefreshTokenRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenService.validateAndGet(dto.getRefreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(dto.getRefreshToken());
        return response;
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createForUser(user);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken.getToken());
        return response;
    }
}
