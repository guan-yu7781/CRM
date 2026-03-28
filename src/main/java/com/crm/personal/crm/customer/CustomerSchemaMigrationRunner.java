package com.crm.personal.crm.customer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomerSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public CustomerSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        dropColumnIfExists("phone");
        dropColumnIfExists("company");
        dropColumnIfExists("preferred_channel");
        dropColumnIfExists("residency_country");
        dropColumnIfExists("relationship_manager");
        dropColumnIfExists("onboarding_stage");
        dropColumnIfExists("email");
        dropColumnIfExists("kyc_status");
        migrateSegmentValues();
    }

    /** Map legacy generic segments to the new industry-aligned values. */
    private void migrateSegmentValues() {
        try {
            jdbcTemplate.execute("update customers set segment = 'COMMERCIAL_BANK'    where segment in ('CORPORATE','WEALTH')");
            jdbcTemplate.execute("update customers set segment = 'PAYMENT_INSTITUTION' where segment = 'SME'");
            jdbcTemplate.execute("update customers set segment = 'COMMERCIAL_BANK'    where segment = 'RETAIL'");
        } catch (Exception ignored) {}
    }

    private void dropColumnIfExists(String columnName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from information_schema.columns " +
                            "where upper(table_name) = 'CUSTOMERS' and upper(column_name) = upper(?)",
                    Integer.class,
                    columnName
            );
            if (count != null && count > 0) {
                jdbcTemplate.execute("alter table customers drop column " + columnName);
            }
        } catch (Exception ignored) {
            // column already absent or DB does not support this DDL — safe to skip
        }
    }
}
