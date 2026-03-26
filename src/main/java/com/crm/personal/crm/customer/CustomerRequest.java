package com.crm.personal.crm.customer;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CustomerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private CustomerType customerType = CustomerType.COMMERCIAL_BANK;

    @NotBlank(message = "CIF number is required")
    private String cifNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private CustomerSegment segment = CustomerSegment.RETAIL;

    private CustomerStatus status = CustomerStatus.LEAD;

    private KycStatus kycStatus = KycStatus.PENDING;

    private RiskLevel riskLevel = RiskLevel.LOW;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
