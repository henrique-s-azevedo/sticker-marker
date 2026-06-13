package com.henrique.stickermarker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends transactional emails via the Brevo (formerly Sendinblue) REST API.
 *
 * <p>Brevo is used instead of SMTP because Railway's shared IP ranges are frequently
 * blocklisted by major email providers. The Brevo API uses its own dedicated sending
 * infrastructure with established domain reputation.</p>
 *
 * <p>All configuration is injected from environment variables:
 * {@code BREVO_API_KEY}, {@code BREVO_FROM_EMAIL}, {@code BREVO_FROM_NAME}.</p>
 */
@Service
public class EmailService {

    private final RestClient restClient = RestClient.create();

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name:Sticker Marker}")
    private String fromName;

    /**
     * Sends a 6-digit one-time verification code to the user's email address.
     * Used as part of the two-step password change flow; the code expires in 15 minutes.
     *
     * @param toEmail the recipient's email address
     * @param code    the 6-digit verification code to include in the message
     * @throws org.springframework.web.client.RestClientException if the Brevo API call fails
     */
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
