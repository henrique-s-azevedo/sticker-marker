package com.henrique.stickermarker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Verifies Google ID tokens by calling Google's public token-info endpoint.
 *
 * <p>This is a lightweight alternative to the full OAuth2 code flow: the frontend
 * performs the Google Sign-In and receives an ID token, which it sends to the backend.
 * The backend verifies the token directly with Google rather than implementing
 * signature verification locally, which avoids managing Google's public key rotation.</p>
 *
 * <p>The {@code aud} (audience) claim is checked against the configured client ID to ensure
 * the token was issued for this application and not stolen from another app.</p>
 */
@Service
public class GoogleTokenVerifier {

    @Value("${app.google.client-id}")
    private String clientId;

    private final RestClient restClient = RestClient.create();

    /**
     * Carries the verified claims extracted from a Google ID token.
     *
     * @param sub   the Google user's unique subject identifier (stable across email changes)
     * @param email the user's email address
     * @param name  the user's display name (may be null if Google did not provide it)
     */
    public record GoogleTokenInfo(String sub, String email, String name) {}

    /**
     * Verifies the given Google ID token by calling Google's token-info endpoint
     * and returns the extracted user claims.
     *
     * @param idToken the raw Google ID token from the frontend
     * @return the verified claims (sub, email, name)
     * @throws IllegalArgumentException if the HTTP call to Google fails,
     *                                  the token is invalid, or the audience does not match
     */
    public GoogleTokenInfo verify(String idToken) {
        Map<?, ?> claims;
        try {
            claims = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        if (claims == null || !clientId.equals(claims.get("aud"))) {
            throw new IllegalArgumentException("Invalid Google token audience");
        }

        return new GoogleTokenInfo(
                (String) claims.get("sub"),
                (String) claims.get("email"),
                (String) claims.get("name")
        );
    }
}
