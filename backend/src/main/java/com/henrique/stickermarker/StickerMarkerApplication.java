package com.henrique.stickermarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Sticker Marker Spring Boot application.
 *
 * <p>Bootstraps the application context, triggers component scanning under
 * {@code com.henrique.stickermarker}, and starts the embedded servlet container.</p>
 */
@SpringBootApplication
public class StickerMarkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StickerMarkerApplication.class, args);
    }
}
