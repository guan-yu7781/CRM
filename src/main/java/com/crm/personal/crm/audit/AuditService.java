package com.crm.personal.crm.audit;

import com.crm.personal.crm.security.AppUser;
import com.crm.personal.crm.security.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public AuditService(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    public void log(String objectType, Long objectId, String objectName, AuditAction action, String changeSummary) {
        try {
            String username = "system";
            String fullName = null;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                username = auth.getName();
                fullName = appUserRepository.findByUsername(username)
                        .map(AppUser::getFullName).orElse(null);
            }

            String ipAddress = null;
            String userAgent = null;
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest req = attrs.getRequest();
                String xff = req.getHeader("X-Forwarded-For");
                ipAddress = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
                userAgent = req.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
            } catch (Exception ignored) {
                // Not in a request context (e.g., called from a scheduled task)
            }

            AuditLog entry = new AuditLog();
            entry.setActorUsername(username);
            entry.setActorFullName(fullName);
            entry.setAction(action);
            entry.setObjectType(objectType);
            entry.setObjectId(objectId);
            entry.setObjectName(objectName);
            entry.setChangeSummary(changeSummary);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            entry.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(entry);
        } catch (Exception e) {
            logger.warn("Audit logging failed for action {} on {}/{}: {}", action, objectType, objectId, e.getMessage());
        }
    }
}
