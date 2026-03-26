package com.crm.personal.crm.activity;

import java.time.LocalDateTime;

public class ActivityResponse {

    private Long id;
    private ActivityType type;
    private String subject;
    private String details;
    private LocalDateTime activityDate;
    private Long customerId;
    private String customerName;
    private Long contactId;
    private String contactName;
    private Long dealId;
    private String dealTitle;
    private String createdBy;
    private LocalDateTime createdAt;

    public static ActivityResponse from(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setType(activity.getType());
        response.setSubject(activity.getSubject());
        response.setDetails(activity.getDetails());
        response.setActivityDate(activity.getActivityDate());
        response.setCustomerId(activity.getCustomer().getId());
        response.setCustomerName(activity.getCustomer().getName());
        if (activity.getContact() != null) {
            response.setContactId(activity.getContact().getId());
            response.setContactName(activity.getContact().getFirstName() + " " + activity.getContact().getLastName());
        }
        if (activity.getDeal() != null) {
            response.setDealId(activity.getDeal().getId());
            response.setDealTitle(activity.getDeal().getTitle());
        }
        response.setCreatedBy(activity.getCreatedBy().getUsername());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDateTime activityDate) {
        this.activityDate = activityDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    public String getDealTitle() {
        return dealTitle;
    }

    public void setDealTitle(String dealTitle) {
        this.dealTitle = dealTitle;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
