package com.crm.personal.crm.reminder;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MaintenanceExpiryRecord {

    private Long id;
    private String projectName;
    private String market;
    private Integer maintenanceYear;
    private LocalDate endDate;
    private BigDecimal amount;
    private String paymentStatus;
    private Long customerId;
    private String customerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public Integer getMaintenanceYear() { return maintenanceYear; }
    public void setMaintenanceYear(Integer maintenanceYear) { this.maintenanceYear = maintenanceYear; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
