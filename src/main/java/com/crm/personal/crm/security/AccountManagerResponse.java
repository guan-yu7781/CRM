package com.crm.personal.crm.security;

public class AccountManagerResponse {

    private final Long id;
    private final String fullName;
    private final String role;
    private final String roleLabel;

    public AccountManagerResponse(Long id, String fullName, String role, String roleLabel) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
        this.roleLabel = roleLabel;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getRoleLabel() { return roleLabel; }
}
