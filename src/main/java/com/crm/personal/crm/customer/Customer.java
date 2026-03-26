package com.crm.personal.crm.customer;

import com.crm.personal.crm.deal.Deal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType = CustomerType.COMMERCIAL_BANK;

    @Column(nullable = false, unique = true)
    private String cifNumber;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerSegment segment = CustomerSegment.RETAIL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.LEAD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferredChannel preferredChannel = PreferredChannel.MOBILE_APP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStage onboardingStage = OnboardingStage.PROSPECT;

    private String residencyCountry;

    private String relationshipManager;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deal> deals = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    public List<Deal> getDeals() {
        return deals;
    }

    public void setDeals(List<Deal> deals) {
        this.deals = deals;
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
