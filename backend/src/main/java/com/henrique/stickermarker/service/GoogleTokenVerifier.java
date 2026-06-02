package com.henrique.stickermarker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GoogleTokenVerifier {

    @Value("${app.google.client-id}")
    private String clientId;

    private final RestClient restClient = RestClient.create();

    public record GoogleTokenInfo(String sub, String email, String name) {}

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
