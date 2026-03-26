package com.crm.personal.crm.customer;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum CustomerType {
    COMMERCIAL_BANK,
    PAYMENT_INSTITUTION,
    CENTRAL_BANK,
    MICROFINANCE_BANK,
    SACCO,
    INDIVIDUAL,
    BUSINESS;

    @JsonCreator
    public static CustomerType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);

        switch (normalized) {
            case "COMMERCIAL_BANK_CUSTOMER":
            case "COMMERCIAL_BANK":
                return COMMERCIAL_BANK;
            case "PAYMENT_CUSTOMER":
            case "PAYMENT_INSTITUTION":
            case "PAYMENT_CLIENT":
            case "PAYMENT_SERVICE_PROVIDER":
                return PAYMENT_INSTITUTION;
            case "CENTRAL_BANK":
                return CENTRAL_BANK;
            case "MICROFINANCE_BANK":
            case "MICRO_FINANCE_BANK":
                return MICROFINANCE_BANK;
            case "SACCO":
                return SACCO;
            case "INDIVIDUAL":
                return INDIVIDUAL;
            case "BUSINESS":
                return BUSINESS;
            default:
                return CustomerType.valueOf(normalized);
        }
    }
}
