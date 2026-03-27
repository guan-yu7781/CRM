package com.crm.personal.crm.security;

import java.util.List;

public class AuthResponse {

    private String token;
    private String username;
    private String role;
    private String roleLabel;
    private String dataScope;
    private List<String> permissions;

    public AuthResponse(String token,
                        String username,
                        String role,
                        String roleLabel,
                        String dataScope,
                        List<String> permissions) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.roleLabel = roleLabel;
        this.dataScope = dataScope;
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
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
}
