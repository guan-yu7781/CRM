package com.crm.personal.crm.security;

import com.crm.personal.crm.audit.AuditAction;
import com.crm.personal.crm.audit.AuditService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessControlService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AccessControlService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                                AuditService auditService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AccessControlRoleResponse> getRoles() {
        return UserRole.assignableRoles().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccessControlUserResponse> getUsers() {
        return appUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getCreatedAt).thenComparing(AppUser::getUsername))
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccessControlUserResponse createUser(AccessControlUserRequest request) {
        validateRole(request.getRole());
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a user.");
        }
        appUserRepository.findByUsername(request.getUsername())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Username already exists.");
                });

        AppUser user = new AppUser();
        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(trimOrNull(request.getEmail()));
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        AccessControlUserResponse result = toUserResponse(appUserRepository.save(user));
        auditService.log("USER", result.getId(), result.getUsername(), AuditAction.CREATE,
                "Created user '" + result.getUsername() + "' with role " + result.getRole());
        return result;
    }

    @Transactional
    public AccessControlUserResponse updateUser(Long id, AccessControlUserRequest request) {
        validateRole(request.getRole());
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        appUserRepository.findByUsername(request.getUsername())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Username already exists.");
                });

        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername().trim());
        user.setEmail(trimOrNull(request.getEmail()));
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        AccessControlUserResponse result = toUserResponse(appUserRepository.save(user));
        auditService.log("USER", id, result.getUsername(), AuditAction.UPDATE,
                "Updated user '" + result.getUsername() + "' (role: " + result.getRole() + ")");
        return result;
    }

    private String trimOrNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void validateRole(UserRole role) {
        if (role == null || !role.isAssignableRole()) {
            throw new IllegalArgumentException("Select a supported business role.");
        }
    }

    private AccessControlRoleResponse toRoleResponse(UserRole role) {
        UserRole effectiveRole = role.getEffectiveRole();
        List<String> permissions = effectiveRole.getPermissions().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        return new AccessControlRoleResponse(
                effectiveRole.name(),
                effectiveRole.getLabel(),
                effectiveRole.getDescription(),
                effectiveRole.getDataScope().name(),
                permissions
        );
    }

    private AccessControlUserResponse toUserResponse(AppUser user) {
        UserRole effectiveRole = user.getRole().getEffectiveRole();
        List<String> permissions = effectiveRole.getPermissions().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        return new AccessControlUserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                effectiveRole.name(),
                effectiveRole.getLabel(),
                effectiveRole.getDataScope().name(),
                permissions,
                user.getCreatedAt()
        );
    }
}
