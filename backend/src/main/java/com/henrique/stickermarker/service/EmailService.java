package com.henrique.stickermarker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient = RestClient.create();

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name:Sticker Marker}")
    private String fromName;

    public void sendPasswordChangeCode(String toEmail, String code) {
        Map<String, Object> body = Map.of(
            "sender", Map.of("name", fromName, "email", fromEmail),
            "to", List.of(Map.of("email", toEmail)),
            "subject", "Verification code - Sticker Marker",
            "textContent", "Your verification code is: " + code + "\n\nThis code expires in 15 minutes."
        );

        restClient.post()
            .uri("https://api.brevo.com/v3/smtp/email")
            .header("api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }
}
