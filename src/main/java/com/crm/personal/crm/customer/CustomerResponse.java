package com.crm.personal.crm.customer;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String name;
    private CustomerType customerType;
    private String cifNumber;
    private CustomerStatus status;
    private RiskLevel riskLevel;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerResponse from(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setCustomerType(customer.getCustomerType());
        response.setCifNumber(customer.getCifNumber());
        response.setStatus(customer.getStatus());
        response.setRiskLevel(customer.getRiskLevel());
        response.setNotes(customer.getNotes());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    public static CustomerResponse from(CustomerRecord customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setCustomerType(customer.getCustomerType());
        response.setCifNumber(customer.getCifNumber());
        response.setStatus(customer.getStatus());
        response.setRiskLevel(customer.getRiskLevel());
        response.setNotes(customer.getNotes());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

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
