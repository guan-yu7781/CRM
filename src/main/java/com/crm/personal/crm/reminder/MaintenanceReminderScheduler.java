package com.crm.personal.crm.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers the maintenance expiry reminder scan daily at 08:00 (configurable via
 * the {@code app.reminder.cron} property / {@code REMINDER_CRON} env var).
 *
 * <p>Reminder windows:</p>
 * <ul>
 *   <li>30 days before expiry — first notice</li>
 *   <li>7 days before expiry  — second notice</li>
 *   <li>1 day before expiry   — urgent notice</li>
 *   <li>On expiry day / 1 day overdue — final notice</li>
 * </ul>
 *
 * <p>Each reminder is sent at most once per maintenance record per type;
 * results are persisted in {@code email_reminder_logs}.</p>
 */
@Component
public class MaintenanceReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceReminderScheduler.class);

    private final MaintenanceReminderService reminderService;

    public MaintenanceReminderScheduler(MaintenanceReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "${app.reminder.cron:0 0 8 * * ?}")
    public void runDailyReminderScan() {
        log.info("=== Maintenance Reminder Scheduler triggered ===");
        try {
            reminderService.processAllReminders();
        } catch (Exception ex) {
            log.error("Maintenance reminder scan failed: {}", ex.getMessage(), ex);
        }
        log.info("=== Maintenance Reminder Scheduler completed ===");
    }
}
