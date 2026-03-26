package com.crm.personal.crm.customer;

import java.time.LocalDateTime;

public class CustomerRecord {

    private Long id;
    private String name;
    private CustomerType customerType;
    private String cifNumber;
    private String email;
    private String phone;
    private String company;
    private CustomerSegment segment;
    private CustomerStatus status;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
    private PreferredChannel preferredChannel;
    private OnboardingStage onboardingStage;
    private String residencyCountry;
    private String relationshipManager;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public String getCifNumber() {
        return cifNumber;
    }

    public void setCifNumber(String cifNumber) {
        this.cifNumber = cifNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public CustomerSegment getSegment() {
        return segment;
    }

    public void setSegment(CustomerSegment segment) {
        this.segment = segment;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public PreferredChannel getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(PreferredChannel preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    public OnboardingStage getOnboardingStage() {
        return onboardingStage;
    }

    public void setOnboardingStage(OnboardingStage onboardingStage) {
        this.onboardingStage = onboardingStage;
    }

    public String getResidencyCountry() {
        return residencyCountry;
    }

    public void setResidencyCountry(String residencyCountry) {
        this.residencyCountry = residencyCountry;
    }

    public String getRelationshipManager() {
        return relationshipManager;
    }

    public void setRelationshipManager(String relationshipManager) {
        this.relationshipManager = relationshipManager;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
