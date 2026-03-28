package com.crm.personal.crm.customer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CustomerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private CustomerType customerType = CustomerType.COMMERCIAL_BANK;

    private String cifNumber;

    private CustomerSegment segment = CustomerSegment.COMMERCIAL_BANK;

    private CustomerStatus status = CustomerStatus.LEAD;

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
