package com.crm.personal.crm.security;

import java.time.LocalDateTime;
import java.util.List;

public class AccessControlUserResponse {

    private final Long id;
    private final String fullName;
    private final String username;
    private final String role;
    private final String roleLabel;
    private final String dataScope;
    private final List<String> permissions;
    private final LocalDateTime createdAt;

    public AccessControlUserResponse(Long id,
                                     String fullName,
                                     String username,
                                     String role,
                                     String roleLabel,
                                     String dataScope,
                                     List<String> permissions,
                                     LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.roleLabel = roleLabel;
        this.dataScope = dataScope;
        this.permissions = permissions;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public String getDataScope() {
        return dataScope;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
