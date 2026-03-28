package com.crm.personal.crm.reminder;

import com.crm.personal.crm.security.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceReminderService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceReminderService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MaintenanceExpiryMapper expiryMapper;
    private final EmailReminderLogRepository logRepository;
    private final EmailService emailService;
    private final AppUserRepository appUserRepository;

    @Value("${app.reminder.enabled:true}")
    private boolean enabled;

    /** Fallback recipients if no CRM user has an email configured. */
    @Value("${app.reminder.recipients:}")
    private String recipientsFallback;

    public MaintenanceReminderService(MaintenanceExpiryMapper expiryMapper,
                                      EmailReminderLogRepository logRepository,
                                      EmailService emailService,
                                      AppUserRepository appUserRepository) {
        this.expiryMapper = expiryMapper;
        this.logRepository = logRepository;
        this.emailService = emailService;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public void processAllReminders() {
        if (!enabled) {
            log.info("Maintenance reminders are disabled (app.reminder.enabled=false). Skipping.");
            return;
        }

        LocalDate today = LocalDate.now();
        log.info("Running maintenance expiry reminder scan for date: {}", today);

        processForDate(today.plusDays(30), ReminderType.THIRTY_DAYS, "【提前30天提醒】维护合同即将到期", 30);
        processForDate(today.plusDays(7),  ReminderType.SEVEN_DAYS,  "【提前7天提醒】维护合同即将到期", 7);
        processForDate(today.plusDays(1),  ReminderType.ONE_DAY,     "【提前1天提醒】维护合同明日到期", 1);
        processExpired(today);
    }

    private void processForDate(LocalDate targetDate, ReminderType type, String subjectPrefix, int daysLeft) {
        List<MaintenanceExpiryRecord> records = expiryMapper.findByEndDate(targetDate);
        for (MaintenanceExpiryRecord record : records) {
            if (logRepository.countByMaintenanceIdAndReminderType(record.getId(), type.name()) > 0) {
                log.debug("Reminder {} already sent for maintenance #{}, skipping.", type, record.getId());
                continue;
            }
            String subject = subjectPrefix + " — " + record.getCustomerName() + " / " + record.getProjectName();
            sendToAllRecipients(record, type, subject, buildBody(record, daysLeft, false));
        }
    }

    private void processExpired(LocalDate today) {
        List<MaintenanceExpiryRecord> records = expiryMapper.findByEndDateBetween(today.minusDays(1), today);
        for (MaintenanceExpiryRecord record : records) {
            if (logRepository.countByMaintenanceIdAndReminderType(record.getId(), ReminderType.EXPIRED.name()) > 0) {
                log.debug("EXPIRED reminder already sent for maintenance #{}, skipping.", record.getId());
                continue;
            }
            boolean isToday = !record.getEndDate().isBefore(today);
            String subjectPrefix = isToday ? "【到期当天提醒】维护合同今日到期" : "【已过期提醒】维护合同已过期1天";
            sendToAllRecipients(record, ReminderType.EXPIRED,
                    subjectPrefix + " — " + record.getCustomerName() + " / " + record.getProjectName(),
                    buildBody(record, 0, true));
        }
    }

    private void sendToAllRecipients(MaintenanceExpiryRecord record, ReminderType type,
                                     String subject, String body) {
        List<String> recipients = resolveRecipients();
        if (recipients.isEmpty()) {
            log.warn("No recipients configured for reminder type {}. Skipping maintenance #{}.", type, record.getId());
            return;
        }
        for (String recipient : recipients) {
            EmailReminderLog entry = new EmailReminderLog();
            entry.setMaintenanceId(record.getId());
            entry.setReminderType(type);
            entry.setRecipientEmail(recipient);
            entry.setSentAt(LocalDateTime.now());
            try {
                emailService.send(recipient, subject, body);
                entry.setStatus(ReminderStatus.SUCCESS);
            } catch (Exception ex) {
                log.error("Failed to send {} reminder for maintenance #{} to {}: {}",
                        type, record.getId(), recipient, ex.getMessage());
                entry.setStatus(ReminderStatus.FAILED);
                entry.setErrorMessage(ex.getMessage());
            }
            logRepository.insert(entry);
        }
    }

    /**
     * Resolves recipient list in priority order:
     * 1. All CRM users who have a non-empty email address
     * 2. Fallback: app.reminder.recipients property (comma-separated)
     */
    private List<String> resolveRecipients() {
        List<String> userEmails = appUserRepository
                .findByEmailIsNotNullAndEmailIsNotOrderByFullNameAsc("")
                .stream()
                .map(u -> u.getEmail().trim())
                .filter(e -> !e.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (!userEmails.isEmpty()) {
            return userEmails;
        }

        // Fallback to configured list
        if (recipientsFallback != null && !recipientsFallback.isBlank()) {
            return Arrays.stream(recipientsFallback.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private String buildBody(MaintenanceExpiryRecord record, int daysLeft, boolean isExpired) {
        StringBuilder sb = new StringBuilder();
        sb.append("维护合同到期提醒\n");
        sb.append("======================\n\n");
        sb.append("客户名称:    ").append(record.getCustomerName()).append("\n");
        sb.append("项目名称:    ").append(record.getProjectName()).append("\n");
        sb.append("市场:        ").append(record.getMarket() != null ? record.getMarket() : "N/A").append("\n");
        sb.append("维护年份:    ").append(record.getMaintenanceYear()).append("\n");
        sb.append("合同到期日:  ").append(record.getEndDate().format(DATE_FMT)).append("\n");
        sb.append("合同金额:    ").append(record.getAmount()).append("\n");
        sb.append("付款状态:    ").append(record.getPaymentStatus()).append("\n\n");

        if (isExpired) {
            sb.append("!!! 该维护合同已到期，请尽快处理续签事宜。\n");
        } else {
            sb.append("距合同到期还有 ").append(daysLeft).append(" 天，请及时安排续签。\n");
        }

        sb.append("\n此邮件由 CRM 系统自动发送，请勿回复。\n");
        return sb.toString();
    }
}
