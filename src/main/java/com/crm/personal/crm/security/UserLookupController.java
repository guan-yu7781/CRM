package com.crm.personal.crm.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lightweight user lookup APIs accessible to any authenticated user.
 * Used by dropdowns that reference CRM users (e.g. account manager picker).
 */
@RestController
@RequestMapping("/api/users")
public class UserLookupController {

    private final AppUserRepository appUserRepository;

    public UserLookupController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Returns all users with Sales Manager, Relationship Manager, or Finance Officer roles.
     * Any authenticated user can call this (needed for the project account manager picker).
     */
    @GetMapping("/account-managers")
    @PreAuthorize("isAuthenticated()")
    public List<AccountManagerResponse> getAccountManagers() {
        List<UserRole> roles = Arrays.asList(
                UserRole.SALES_MANAGER,
                UserRole.RELATIONSHIP_MANAGER,
                UserRole.FINANCE_OFFICER
        );
        return appUserRepository.findByRoleInOrderByFullNameAsc(roles).stream()
                .map(user -> {
                    UserRole effectiveRole = user.getRole().getEffectiveRole();
                    return new AccountManagerResponse(
                            user.getId(),
                            user.getFullName(),
                            effectiveRole.name(),
                            effectiveRole.getLabel()
                    );
                })
                .collect(Collectors.toList());
    }
}
