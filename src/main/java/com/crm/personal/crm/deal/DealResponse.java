package com.crm.personal.crm.deal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DealResponse {

    private Long id;
    private String title;
    private BigDecimal amount;
    private DealStage stage;
    private OpportunityType opportunityType;
    private LocalDate expectedCloseDate;
    private String notes;
    private Long customerId;
    private String customerName;
    private Long convertedProjectId;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DealResponse from(Deal deal) {
        DealResponse response = new DealResponse();
        response.setId(deal.getId());
        response.setTitle(deal.getTitle());
        response.setAmount(deal.getAmount());
        response.setStage(deal.getStage());
        response.setOpportunityType(deal.getOpportunityType());
        response.setExpectedCloseDate(deal.getExpectedCloseDate());
        response.setNotes(deal.getNotes());
        response.setCustomerId(deal.getCustomer().getId());
        response.setCustomerName(deal.getCustomer().getName());
        response.setConvertedProjectId(null);
        response.setConvertedAt(null);
        response.setCreatedAt(deal.getCreatedAt());
        response.setUpdatedAt(deal.getUpdatedAt());
        return response;
    }

    public static DealResponse from(DealRecord deal) {
        DealResponse response = new DealResponse();
        response.setId(deal.getId());
        response.setTitle(deal.getTitle());
        response.setAmount(deal.getAmount());
        response.setStage(deal.getStage());
        response.setOpportunityType(deal.getOpportunityType());
        response.setExpectedCloseDate(deal.getExpectedCloseDate());
        response.setNotes(deal.getNotes());
        response.setCustomerId(deal.getCustomerId());
        response.setCustomerName(deal.getCustomerName());
        response.setConvertedProjectId(deal.getConvertedProjectId());
        response.setConvertedAt(deal.getConvertedAt());
        response.setCreatedAt(deal.getCreatedAt());
        response.setUpdatedAt(deal.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DealStage getStage() {
        return stage;
    }

    public void setStage(DealStage stage) {
        this.stage = stage;
    }

    public OpportunityType getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(OpportunityType opportunityType) {
        this.opportunityType = opportunityType;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Long getConvertedProjectId() {
        return convertedProjectId;
    }

    public void setConvertedProjectId(Long convertedProjectId) {
        this.convertedProjectId = convertedProjectId;
    }

    public LocalDateTime getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(LocalDateTime convertedAt) {
        this.convertedAt = convertedAt;
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
