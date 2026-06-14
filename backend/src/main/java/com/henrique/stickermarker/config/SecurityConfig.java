package com.henrique.stickermarker.config;

import com.henrique.stickermarker.security.CustomUserDetailsService;
import com.henrique.stickermarker.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration for the application.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li><strong>Stateless sessions</strong> — no HTTP session is created; every request is
 *       authenticated independently via JWT, matching the SPA + mobile-ready architecture.</li>
 *   <li><strong>CSRF disabled</strong> — safe because the API is stateless (no cookies for auth)
 *       and CORS already restricts cross-origin requests to the configured origin.</li>
 *   <li><strong>CORS origin from config</strong> — {@code app.cors.allowed-origins} is read from
 *       environment variables, so local dev and production can use different origins without
 *       code changes.</li>
 *   <li><strong>JWT filter before UsernamePasswordAuthenticationFilter</strong> — ensures the
 *       security context is populated from the token before Spring's default auth processing runs.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    /** Comma-separated list of allowed CORS origins, injected from environment. */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Password encoder bean using BCrypt. Exposed as a bean so it can be injected
     * wherever password hashing is needed (e.g. {@link com.henrique.stickermarker.service.AuthService}).
     *
     * @return BCrypt encoder with default strength (10 rounds)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider that delegates user lookup to {@link CustomUserDetailsService}
     * and password verification to {@link BCryptPasswordEncoder}.
     *
     * @return configured DAO authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so it can be injected into
     * {@link com.henrique.stickermarker.service.AuthService} for programmatic authentication
     * during the login flow.
     *
     * @param config Spring's authentication configuration
     * @return the authentication manager
     * @throws Exception if the manager cannot be built
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration that allows the configured frontend origins to call the API.
     * {@code allowCredentials} is intentionally {@code false} because authentication
     * uses the {@code Authorization} header (Bearer token), not cookies.
     *
     * @return the CORS configuration source applied to all routes
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Defines the security filter chain applied to all HTTP requests.
     *
     * <p>Public routes ({@code /auth/**}, {@code /actuator/**}) are permitted without
     * a token. All other routes require a valid JWT. A 401 is returned (rather than a
     * redirect) because this is a REST API consumed by an SPA — redirect-based auth flows
     * are not appropriate here.</p>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured security filter chain
     * @throws Exception if the filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/actuator/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
