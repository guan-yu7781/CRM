package com.crm.personal.crm.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Creates the {@code email_reminder_logs} table on startup if it does not exist.
 * Works across H2, MySQL, and PostgreSQL.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class EmailReminderSchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailReminderSchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public EmailReminderSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        createTableIfAbsent();
        createIndexIfAbsent();
    }

    private void createTableIfAbsent() {
        try {
            jdbcTemplate.execute(
                    "create table if not exists email_reminder_logs (" +
                    "  id              bigint        not null auto_increment primary key," +
                    "  maintenance_id  bigint        not null," +
                    "  reminder_type   varchar(20)   not null," +
                    "  status          varchar(10)   not null," +
                    "  recipient_email varchar(255)," +
                    "  error_message   text," +
                    "  sent_at         datetime      not null" +
                    ")"
            );
            log.info("email_reminder_logs table ensured.");
        } catch (Exception ex) {
            // PostgreSQL uses SERIAL / BIGSERIAL; fall back to a compatible DDL
            try {
                jdbcTemplate.execute(
                        "create table if not exists email_reminder_logs (" +
                        "  id              bigserial     primary key," +
                        "  maintenance_id  bigint        not null," +
                        "  reminder_type   varchar(20)   not null," +
                        "  status          varchar(10)   not null," +
                        "  recipient_email varchar(255)," +
                        "  error_message   text," +
                        "  sent_at         timestamp     not null" +
                        ")"
                );
                log.info("email_reminder_logs table ensured (PostgreSQL DDL).");
            } catch (Exception ex2) {
                log.warn("Could not create email_reminder_logs table: {}", ex2.getMessage());
            }
        }
    }

    private void createIndexIfAbsent() {
        try {
            jdbcTemplate.execute(
                    "create index if not exists idx_email_reminder_lookup " +
                    "on email_reminder_logs(maintenance_id, reminder_type)"
            );
        } catch (Exception ignored) {
            // Index may already exist or DB doesn't support IF NOT EXISTS for indexes — safe to ignore
        }
    }
}
