package com.crm.personal.crm.security;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum UserRole {
    SUPER_ADMIN(
            "Super Admin",
            "Owns platform security, role model, and all CRM operations.",
            DataScope.ALL,
            true,
            EnumSet.allOf(UserPermission.class)
    ),
    CRM_ADMIN(
            "CRM Admin",
            "Maintains customer master data, project records, and operational data quality.",
            DataScope.ALL,
            true,
            EnumSet.of(
                    UserPermission.CUSTOMER_VIEW, UserPermission.CUSTOMER_CREATE, UserPermission.CUSTOMER_EDIT, UserPermission.CUSTOMER_DELETE,
                    UserPermission.CONTACT_VIEW, UserPermission.CONTACT_CREATE, UserPermission.CONTACT_EDIT, UserPermission.CONTACT_DELETE,
                    UserPermission.DEAL_VIEW, UserPermission.DEAL_CREATE, UserPermission.DEAL_EDIT, UserPermission.DEAL_DELETE, UserPermission.DEAL_CONVERT_TO_PROJECT,
                    UserPermission.PROJECT_VIEW, UserPermission.PROJECT_CREATE, UserPermission.PROJECT_EDIT, UserPermission.PROJECT_DELETE, UserPermission.PROJECT_VIEW_FINANCIALS,
                    UserPermission.MAINTENANCE_VIEW, UserPermission.MAINTENANCE_CREATE, UserPermission.MAINTENANCE_EDIT, UserPermission.MAINTENANCE_DELETE,
                    UserPermission.MAINTENANCE_UPDATE_PAYMENT, UserPermission.MAINTENANCE_UPDATE_RENEW,
                    UserPermission.TASK_VIEW, UserPermission.TASK_CREATE, UserPermission.TASK_EDIT, UserPermission.TASK_DELETE,
                    UserPermission.ACTIVITY_VIEW, UserPermission.ACTIVITY_CREATE, UserPermission.ACTIVITY_EDIT, UserPermission.ACTIVITY_DELETE,
                    UserPermission.ACCESS_CONTROL_VIEW
            )
    ),
    SALES_MANAGER(
            "Sales Manager",
            "Oversees pipeline performance, approves won opportunities, and monitors team delivery.",
            DataScope.TEAM,
            true,
            EnumSet.of(
                    UserPermission.CUSTOMER_VIEW,
                    UserPermission.CONTACT_VIEW,
                    UserPermission.DEAL_VIEW, UserPermission.DEAL_CREATE, UserPermission.DEAL_EDIT, UserPermission.DEAL_CONVERT_TO_PROJECT,
                    UserPermission.PROJECT_VIEW, UserPermission.PROJECT_CREATE, UserPermission.PROJECT_EDIT, UserPermission.PROJECT_VIEW_FINANCIALS,
                    UserPermission.MAINTENANCE_VIEW,
                    UserPermission.TASK_VIEW, UserPermission.TASK_CREATE, UserPermission.TASK_EDIT,
                    UserPermission.ACTIVITY_VIEW, UserPermission.ACTIVITY_CREATE, UserPermission.ACTIVITY_EDIT
            )
    ),
    RELATIONSHIP_MANAGER(
            "Relationship Manager",
            "Owns day-to-day customer coverage, stakeholder management, and opportunity progression.",
            DataScope.OWN,
            true,
            EnumSet.of(
                    UserPermission.CUSTOMER_VIEW, UserPermission.CUSTOMER_CREATE, UserPermission.CUSTOMER_EDIT,
                    UserPermission.CONTACT_VIEW, UserPermission.CONTACT_CREATE, UserPermission.CONTACT_EDIT,
                    UserPermission.DEAL_VIEW, UserPermission.DEAL_CREATE, UserPermission.DEAL_EDIT,
                    UserPermission.PROJECT_VIEW, UserPermission.PROJECT_CREATE, UserPermission.PROJECT_EDIT,
                    UserPermission.MAINTENANCE_VIEW, UserPermission.MAINTENANCE_CREATE, UserPermission.MAINTENANCE_EDIT,
                    UserPermission.TASK_VIEW, UserPermission.TASK_CREATE, UserPermission.TASK_EDIT,
                    UserPermission.ACTIVITY_VIEW, UserPermission.ACTIVITY_CREATE, UserPermission.ACTIVITY_EDIT
            )
    ),
    FINANCE_OFFICER(
            "Finance Officer",
            "Controls payment visibility, renewal follow-up, and project financial review.",
            DataScope.ALL,
            true,
            EnumSet.of(
                    UserPermission.CUSTOMER_VIEW,
                    UserPermission.CONTACT_VIEW,
                    UserPermission.DEAL_VIEW,
                    UserPermission.PROJECT_VIEW, UserPermission.PROJECT_VIEW_FINANCIALS,
                    UserPermission.MAINTENANCE_VIEW, UserPermission.MAINTENANCE_UPDATE_PAYMENT, UserPermission.MAINTENANCE_UPDATE_RENEW,
                    UserPermission.TASK_VIEW,
                    UserPermission.ACTIVITY_VIEW
            )
    ),
    ADMIN(
            "Super Admin",
            "Legacy admin role kept for backward compatibility.",
            DataScope.ALL,
            false,
            EnumSet.allOf(UserPermission.class)
    ),
    SALES(
            "Relationship Manager",
            "Legacy sales role kept for backward compatibility.",
            DataScope.OWN,
            false,
            EnumSet.of(
                    UserPermission.CUSTOMER_VIEW, UserPermission.CUSTOMER_CREATE, UserPermission.CUSTOMER_EDIT,
                    UserPermission.CONTACT_VIEW, UserPermission.CONTACT_CREATE, UserPermission.CONTACT_EDIT,
                    UserPermission.DEAL_VIEW, UserPermission.DEAL_CREATE, UserPermission.DEAL_EDIT,
                    UserPermission.PROJECT_VIEW, UserPermission.PROJECT_CREATE, UserPermission.PROJECT_EDIT,
                    UserPermission.MAINTENANCE_VIEW, UserPermission.MAINTENANCE_CREATE, UserPermission.MAINTENANCE_EDIT,
                    UserPermission.TASK_VIEW, UserPermission.TASK_CREATE, UserPermission.TASK_EDIT,
                    UserPermission.ACTIVITY_VIEW, UserPermission.ACTIVITY_CREATE, UserPermission.ACTIVITY_EDIT
            )
    );

    private final String label;
    private final String description;
    private final DataScope dataScope;
    private final boolean assignableRole;
    private final Set<UserPermission> permissions;

    UserRole(String label,
             String description,
             DataScope dataScope,
             boolean assignableRole,
             Set<UserPermission> permissions) {
        this.label = label;
        this.description = description;
        this.dataScope = dataScope;
        this.assignableRole = assignableRole;
        this.permissions = permissions;
    }

    public String getLabel() {
        return getEffectiveRole().label;
    }

    public String getDescription() {
        return getEffectiveRole().description;
    }

    public DataScope getDataScope() {
        return getEffectiveRole().dataScope;
    }

    public boolean isAssignableRole() {
        return getEffectiveRole() == this && assignableRole;
    }

    public Set<UserPermission> getPermissions() {
        return getEffectiveRole().permissions;
    }

    public UserRole getEffectiveRole() {
        if (this == ADMIN) {
            return SUPER_ADMIN;
        }
        if (this == SALES) {
            return RELATIONSHIP_MANAGER;
        }
        return this;
    }

    public static List<UserRole> assignableRoles() {
        return List.of(SUPER_ADMIN, CRM_ADMIN, SALES_MANAGER, RELATIONSHIP_MANAGER, FINANCE_OFFICER);
    }
}
