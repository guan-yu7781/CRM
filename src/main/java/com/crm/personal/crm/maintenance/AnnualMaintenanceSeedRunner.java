package com.crm.personal.crm.maintenance;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.project.ProjectRecord;
import com.crm.personal.crm.project.ProjectService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class AnnualMaintenanceSeedRunner implements ApplicationRunner {

    private final AnnualMaintenanceMapper annualMaintenanceMapper;
    private final CustomerService customerService;
    private final ProjectService projectService;
    private final JdbcTemplate jdbcTemplate;

    public AnnualMaintenanceSeedRunner(AnnualMaintenanceMapper annualMaintenanceMapper,
                                       CustomerService customerService,
                                       ProjectService projectService,
                                       JdbcTemplate jdbcTemplate) {
        this.annualMaintenanceMapper = annualMaintenanceMapper;
        this.customerService = customerService;
        this.projectService = projectService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureProjectIdColumn();
        ensureRenewStatusColumn();
        CustomerRecord customer = customerService.findFirstCustomerRecord();
        if (customer == null) {
            return;
        }
        ProjectRecord project = projectService.findOrCreateProjectForMaintenance(customer.getId(), "NCBA Kenya MSL", "Kenya");
        seedRecord(customer, project, 1, LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 14), PaymentStatus.PAID, new BigDecimal("18500.00"));
        seedRecord(customer, project, 2, LocalDate.of(2025, 2, 1), LocalDate.of(2026, 1, 31), PaymentStatus.NOT_PAID, new BigDecimal("21000.00"));
        seedRecord(customer, project, 3, LocalDate.of(2026, 3, 1), LocalDate.of(2027, 2, 28), PaymentStatus.PAID, new BigDecimal("23000.00"));
    }

    private void seedRecord(CustomerRecord customer,
                            ProjectRecord project,
                            int year,
                            LocalDate startDate,
                            LocalDate endDate,
                            PaymentStatus paymentStatus,
                            BigDecimal amount) {
        if (annualMaintenanceMapper.countByCustomerIdAndProjectIdAndMaintenanceYear(
                customer.getId(), project.getId(), year) > 0) {
            return;
        }

        AnnualMaintenanceRecord record = new AnnualMaintenanceRecord();
        record.setProjectId(project.getId());
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
        record.setProjectName(project.getProjectName());
        record.setMarket(project.getMarket());
        record.setMaintenanceYear(year);
        record.setAmount(amount);
        record.setStartDate(startDate);
        record.setEndDate(endDate);
        record.setPaymentStatus(paymentStatus);
        record.setRenewStatus(year == 2 ? RenewStatus.NOT_RENEWED : RenewStatus.RENEWED);
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        annualMaintenanceMapper.insert(record);
    }

    private void ensureProjectIdColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from information_schema.columns " +
                        "where table_schema = database() and table_name = 'annual_maintenance' and column_name = 'project_id'",
                Integer.class
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("alter table annual_maintenance add column project_id bigint");
        }
    }

    private void ensureRenewStatusColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from information_schema.columns " +
                        "where table_schema = database() and table_name = 'annual_maintenance' and column_name = 'renew_status'",
                Integer.class
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("alter table annual_maintenance add column renew_status varchar(32) default 'NOT_RENEWED'");
        }
        jdbcTemplate.update("update annual_maintenance set renew_status = 'NOT_RENEWED' where renew_status is null");
    }
}
