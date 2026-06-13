package com.henrique.stickermarker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that authenticates incoming requests by validating the JWT in the
 * {@code Authorization: Bearer <token>} header.
 *
 * <p>Runs once per request ({@link OncePerRequestFilter}). If the token is valid,
 * it builds a {@link UsernamePasswordAuthenticationToken} and populates the
 * {@link SecurityContextHolder} so downstream controllers can trust the authentication.</p>
 *
 * <p><strong>Convention:</strong> the {@code userId} (Long) extracted from the token
 * is stored in {@code authToken.details}. All authenticated controllers retrieve the
 * current user via {@code (Long) authentication.getDetails()} — this avoids a database
 * lookup on every request compared to resolving the user from the email subject.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Extracts and validates the Bearer token, then populates the security context.
     * Requests without a valid token pass through unauthenticated — Spring Security's
     * authorization rules decide whether the endpoint requires authentication.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Skip if token is invalid or context is already populated (e.g. re-entrant filter calls)
        if (!jwtUtil.validateToken(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.getEmailFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Store userId in details so controllers can retrieve it without a DB roundtrip
        authToken.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
