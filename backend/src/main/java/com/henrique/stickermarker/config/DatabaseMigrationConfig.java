package com.henrique.stickermarker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    CommandLineRunner fixMessageTypeConstraint(JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute("ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_message_type_check");
            jdbc.execute("""
                ALTER TABLE messages ADD CONSTRAINT messages_message_type_check
                CHECK (message_type IN ('CHAT','TRADE_PROPOSAL','TRADE_RESPONSE','TRADE_CONFIRMED','TRADE_REJECTED','SELL_PROPOSAL','BUY_PROPOSAL'))
                """);
        };
    }
}
