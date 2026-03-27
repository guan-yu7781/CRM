package com.crm.personal.crm.security;

import java.util.List;

public class AccessControlRoleResponse {

    private final String role;
    private final String label;
    private final String description;
    private final String dataScope;
    private final List<String> permissions;

    public AccessControlRoleResponse(String role,
                                     String label,
                                     String description,
                                     String dataScope,
                                     List<String> permissions) {
        this.role = role;
        this.label = label;
        this.description = description;
        this.dataScope = dataScope;
        this.permissions = permissions;
    }

    public String getRole() {
        return role;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getDataScope() {
        return dataScope;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
