package com.crm.personal.crm.maintenance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnnualMaintenanceResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String market;
    private Integer maintenanceYear;
    private BigDecimal amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private PaymentStatus paymentStatus;
    private RenewStatus renewStatus;
    private Long customerId;
    private String customerName;
    private boolean expired;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnnualMaintenanceResponse from(AnnualMaintenanceRecord maintenance) {
        AnnualMaintenanceResponse response = new AnnualMaintenanceResponse();
        response.setId(maintenance.getId());
        response.setProjectId(maintenance.getProjectId());
        response.setProjectName(maintenance.getProjectName());
        response.setMarket(maintenance.getMarket());
        response.setMaintenanceYear(maintenance.getMaintenanceYear());
        response.setAmount(maintenance.getAmount());
        response.setCurrency(maintenance.getCurrency());
        response.setStartDate(maintenance.getStartDate());
        response.setEndDate(maintenance.getEndDate());
        response.setPaymentStatus(maintenance.getPaymentStatus());
        response.setRenewStatus(maintenance.getRenewStatus());
        response.setCustomerId(maintenance.getCustomerId());
        response.setCustomerName(maintenance.getCustomerName());
        boolean expired = maintenance.getEndDate().isBefore(LocalDate.now());
        response.setExpired(expired);
        response.setStatus(expired ? "EXPIRED" : "ACTIVE");
        response.setCreatedAt(maintenance.getCreatedAt());
        response.setUpdatedAt(maintenance.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Integer getMaintenanceYear() {
        return maintenanceYear;
    }

    public void setMaintenanceYear(Integer maintenanceYear) {
        this.maintenanceYear = maintenanceYear;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public RenewStatus getRenewStatus() {
        return renewStatus;
    }

    public void setRenewStatus(RenewStatus renewStatus) {
        this.renewStatus = renewStatus;
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

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
