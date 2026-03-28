package com.crm.personal.crm.audit;

import com.crm.personal.crm.security.AppUser;
import com.crm.personal.crm.security.AppUserRepository;
import com.crm.personal.crm.security.UserRole;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public AuditLogController(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public Map<String, Object> getAuditLogs(
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {

        AppUser currentUser = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isSuperAdmin = currentUser.getRole().getEffectiveRole() == UserRole.SUPER_ADMIN;

        Specification<AuditLog> spec = Specification.where(null);

        if (!isSuperAdmin) {
            String self = currentUser.getUsername();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actorUsername"), self));
        } else if (actorUsername != null && !actorUsername.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actorUsername"), actorUsername));
        }

        if (objectType != null && !objectType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("objectType"), objectType));
        }

        if (action != null && !action.isBlank()) {
            try {
                AuditAction auditAction = AuditAction.valueOf(action);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), auditAction));
            } catch (IllegalArgumentException ignored) {
                // Invalid action value — return empty
                spec = spec.and((root, query, cb) -> cb.disjunction());
            }
        }

        if (startDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }

        if (endDate != null) {
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), end));
        }

        int clampedSize = Math.min(Math.max(size, 1), 200);
        PageRequest pageable = PageRequest.of(page, clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponse> content = result.getContent().stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("page", page);
        response.put("size", clampedSize);
        response.put("isSuperAdmin", isSuperAdmin);
        return response;
    }
}
