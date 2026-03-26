package com.crm.personal.crm.maintenance;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AnnualMaintenanceRequest {

    @NotNull(message = "Project id is required")
    private Long projectId;

    @NotNull(message = "Maintenance year is required")
    private Integer maintenanceYear;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    @NotNull(message = "Customer id is required")
    private Long customerId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
