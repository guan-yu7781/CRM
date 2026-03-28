package com.crm.personal.crm.project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectRecord {

    private Long id;
    private String projectName;
    private String market;
    private String currency = "USD";
    private BigDecimal licenseAmount;
    private BigDecimal implementationAmount;
    private BigDecimal taxRate;
    private ProjectStatus status;
    private Long sourceDealId;
    private Long accountManagerId;
    private String accountManagerName;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency == null ? "USD" : currency;
    }

    public BigDecimal getLicenseAmount() {
        return licenseAmount;
    }

    public void setLicenseAmount(BigDecimal licenseAmount) {
        this.licenseAmount = licenseAmount;
    }

    public BigDecimal getImplementationAmount() {
        return implementationAmount;
    }

    public void setImplementationAmount(BigDecimal implementationAmount) {
        this.implementationAmount = implementationAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Long getSourceDealId() {
        return sourceDealId;
    }

    public void setSourceDealId(Long sourceDealId) {
        this.sourceDealId = sourceDealId;
    }

    public Long getAccountManagerId() {
        return accountManagerId;
    }

    public void setAccountManagerId(Long accountManagerId) {
        this.accountManagerId = accountManagerId;
    }

    public String getAccountManagerName() {
        return accountManagerName;
    }

    public void setAccountManagerName(String accountManagerName) {
        this.accountManagerName = accountManagerName;
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
