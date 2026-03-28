package com.crm.personal.crm.project;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Market is required")
    private String market;

    private String currency = "USD";

    @NotNull(message = "License amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "License amount must be zero or greater")
    private BigDecimal licenseAmount;

    @NotNull(message = "Implementation amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Implementation amount must be zero or greater")
    private BigDecimal implementationAmount;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax rate must be zero or greater")
    private BigDecimal taxRate;

    private ProjectStatus status = ProjectStatus.UNSIGNED_CONTRACT;

    private Long accountManagerId;

    @NotNull(message = "Customer id is required")
    private Long customerId;

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
        this.currency = currency;
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

    public Long getAccountManagerId() {
        return accountManagerId;
    }

    public void setAccountManagerId(Long accountManagerId) {
        this.accountManagerId = accountManagerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
