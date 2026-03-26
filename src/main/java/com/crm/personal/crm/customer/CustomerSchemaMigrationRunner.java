package com.crm.personal.crm.customer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
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
    }

    private void dropColumnIfExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from information_schema.columns " +
                        "where table_schema = database() and table_name = 'customers' and column_name = ?",
                Integer.class,
                columnName
        );
        if (count != null && count > 0) {
            jdbcTemplate.execute("alter table customers drop column " + columnName);
        }
    }
}
