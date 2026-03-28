package com.crm.personal.crm.customer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Generates the next available CIF number for a given CustomerType.
 *
 * Format:  {PREFIX}-{YEAR}-{SEQUENCE:04d}
 * Example: CB-2026-0003
 *
 * Thread-safety: sequence extraction queries the live DB on each call.
 * Duplicate-key protection is handled by the unique index on customers.cif_number
 * and the duplicate-check in CustomerService.createCustomer.
 */
@Component
public class CifNumberGenerator {

    private static final Map<CustomerType, String> PREFIX = Map.of(
        CustomerType.COMMERCIAL_BANK,    "CB",
        CustomerType.PAYMENT_INSTITUTION, "PI",
        CustomerType.CENTRAL_BANK,       "CBK",
        CustomerType.MICROFINANCE_BANK,  "MFB",
        CustomerType.SACCO,              "SACCO",
        CustomerType.INDIVIDUAL,         "IND",
        CustomerType.BUSINESS,           "BIZ"
    );

    private final JdbcTemplate jdbc;

    public CifNumberGenerator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns the next CIF number for the given type (does not persist it). */
    public String next(CustomerType type) {
        String prefix = PREFIX.getOrDefault(type, "CIF");
        String year   = String.valueOf(Year.now().getValue());
        String pattern = prefix + "-" + year + "-%";

        // Find all existing CIF numbers that match the prefix+year pattern
        List<String> existing = jdbc.queryForList(
            "select cif_number from customers where cif_number like ?",
            String.class, pattern
        );

        int maxSeq = existing.stream()
            .mapToInt(cif -> {
                try {
                    // Extract the sequence part after the second dash
                    String[] parts = cif.split("-");
                    return Integer.parseInt(parts[parts.length - 1]);
                } catch (Exception e) {
                    return 0;
                }
            })
            .max()
            .orElse(0);

        return String.format("%s-%s-%04d", prefix, year, maxSeq + 1);
    }

    /** Returns the prefix string for a given CustomerType. */
    public static String prefixFor(CustomerType type) {
        return PREFIX.getOrDefault(type, "CIF");
    }
}
