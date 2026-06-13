package com.henrique.stickermarker.security;

import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security {@link UserDetailsService} implementation that loads users by email.
 *
 * <p>Called by {@link JwtAuthenticationFilter} to build the {@link UserDetails} object
 * placed in the {@link org.springframework.security.core.context.SecurityContextHolder}.
 * Using email as the username aligns with the application's authentication model
 * (email + password and Google OAuth both identify users by email).</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a {@link UserDetails} instance by the user's email address.
     * All authenticated users are assigned the {@code USER} role regardless of
     * any future role differentiation — role-based access is not currently in use.
     *
     * @param email the email address used as the Spring Security username
     * @return a populated {@link UserDetails} object
     * @throws UsernameNotFoundException if no account is found for the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
