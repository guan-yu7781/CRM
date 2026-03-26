package com.crm.personal.crm.customer;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum CustomerType {
    COMMERCIAL_BANK,
    PAYMENT_CUSTOMER,
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
            case "商业银行客户":
            case "COMMERCIAL_BANK_CUSTOMER":
            case "COMMERCIAL_BANK":
                return COMMERCIAL_BANK;
            case "支付客户":
            case "PAYMENT_CLIENT":
            case "PAYMENT_CUSTOMER":
                return PAYMENT_CUSTOMER;
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
