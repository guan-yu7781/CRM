package com.crm.personal.crm.reminder;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for inspecting reminder send history and triggering a manual scan.
 */
@RestController
@RequestMapping("/api/reminders")
public class EmailReminderLogController {

    private final EmailReminderLogRepository logRepository;
    private final MaintenanceReminderService reminderService;

    public EmailReminderLogController(EmailReminderLogRepository logRepository,
                                      MaintenanceReminderService reminderService) {
        this.logRepository = logRepository;
        this.reminderService = reminderService;
    }

    /** List all reminder logs (latest first). */
    @GetMapping
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW')")
    public List<EmailReminderLog> listAll() {
        return logRepository.findAllOrderBySentAtDesc();
    }

    /** List reminder logs for a specific maintenance record. */
    @GetMapping("/maintenance/{maintenanceId}")
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW')")
    public List<EmailReminderLog> listByMaintenance(@PathVariable Long maintenanceId) {
        return logRepository.findByMaintenanceIdOrderBySentAtDesc(maintenanceId);
    }

    /**
     * Manually trigger the reminder scan immediately.
     * Requires MAINTENANCE_DELETE authority (held by SUPER_ADMIN and CRM_ADMIN).
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasAuthority('MAINTENANCE_DELETE')")
    public ResponseEntity<Map<String, Object>> triggerNow() {
        LocalDateTime started = LocalDateTime.now();
        reminderService.processAllReminders();
        return ResponseEntity.ok(Map.of(
                "message", "Reminder scan completed",
                "triggeredAt", started.toString()
        ));
    }
}
