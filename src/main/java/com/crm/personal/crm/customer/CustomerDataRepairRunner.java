package com.crm.personal.crm.customer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public CustomerDataRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.update(
                "update customers set customer_type = ? where customer_type = ?",
                CustomerType.PAYMENT_INSTITUTION.name(),
                "PAYMENT_CUSTOMER"
        );
        jdbcTemplate.update(
                "update customers set customer_type = ? where customer_type is null or trim(customer_type) = ''",
                CustomerType.COMMERCIAL_BANK.name()
        );
        jdbcTemplate.update(
                "update customers set segment = ? where segment is null or trim(segment) = ''",
                CustomerSegment.RETAIL.name()
        );
        jdbcTemplate.update(
                "update customers set status = ? where status is null or trim(status) = ''",
                CustomerStatus.LEAD.name()
        );
        jdbcTemplate.update(
                "update customers set kyc_status = ? where kyc_status is null or trim(kyc_status) = ''",
                KycStatus.PENDING.name()
        );
        jdbcTemplate.update(
                "update customers set risk_level = ? where risk_level is null or trim(risk_level) = ''",
                RiskLevel.LOW.name()
        );
    }
}
