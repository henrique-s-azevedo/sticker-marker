package com.henrique.stickermarker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Applies DDL fixes at startup that Hibernate's {@code ddl-auto=update} cannot handle.
 *
 * <p>The project deliberately avoids a migration tool (Flyway / Liquibase) in favor of
 * {@code ddl-auto=update}. However, Hibernate cannot modify existing CHECK constraints —
 * it only adds missing columns. This config patches constraints that became stale after
 * the {@link com.henrique.stickermarker.model.MessageType} enum was extended with new values.</p>
 *
 * <p>The approach is idempotent: the constraint is dropped first (if it exists) before
 * being re-created with the full set of allowed values, making it safe to run on every
 * application startup.</p>
 */
@Configuration
public class DatabaseMigrationConfig {

    /**
     * Drops and re-creates the {@code messages_message_type_check} constraint to keep it
     * in sync with the current set of {@link com.henrique.stickermarker.model.MessageType}
     * enum values. This is needed because PostgreSQL CHECK constraints are not automatically
     * updated when new enum values are added via {@code ddl-auto=update}.
     *
     * @param jdbc the JDBC template used to execute the DDL statements
     * @return a {@link CommandLineRunner} that applies the fix on every startup
     */
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
