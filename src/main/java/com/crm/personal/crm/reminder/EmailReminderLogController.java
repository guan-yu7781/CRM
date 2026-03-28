package com.crm.personal.crm.reminder;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
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
    private final EmailService emailService;

    public EmailReminderLogController(EmailReminderLogRepository logRepository,
                                      MaintenanceReminderService reminderService,
                                      EmailService emailService) {
        this.logRepository = logRepository;
        this.reminderService = reminderService;
        this.emailService = emailService;
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

    /**
     * Send a one-off test email to verify SMTP connectivity.
     * Requires MAINTENANCE_DELETE authority.
     */
    @PostMapping("/test-email")
    @PreAuthorize("hasAuthority('MAINTENANCE_DELETE')")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestParam String to) {
        try {
            emailService.send(
                    to,
                    "【FinLink CRM】邮件发送测试",
                    "这是一封来自 FinLink CRM 系统的测试邮件，确认 SMTP 配置工作正常。\n\n如您收到此邮件，说明后台邮件发送功能已正常运行。"
            );
            return ResponseEntity.ok(Map.of("success", true, "to", to, "message", "Test email sent successfully."));
        } catch (MailException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
