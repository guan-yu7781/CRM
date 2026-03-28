package com.crm.personal.crm.audit;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String actorUsername;
    private String actorFullName;
    private String action;
    private String objectType;
    private Long objectId;
    private String objectName;
    private String changeSummary;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.id = log.getId();
        r.actorUsername = log.getActorUsername();
        r.actorFullName = log.getActorFullName();
        r.action = log.getAction().name();
        r.objectType = log.getObjectType();
        r.objectId = log.getObjectId();
        r.objectName = log.getObjectName();
        r.changeSummary = log.getChangeSummary();
        r.ipAddress = log.getIpAddress();
        r.userAgent = log.getUserAgent();
        r.createdAt = log.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getActorUsername() { return actorUsername; }
    public String getActorFullName() { return actorFullName; }
    public String getAction() { return action; }
    public String getObjectType() { return objectType; }
    public Long getObjectId() { return objectId; }
    public String getObjectName() { return objectName; }
    public String getChangeSummary() { return changeSummary; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
