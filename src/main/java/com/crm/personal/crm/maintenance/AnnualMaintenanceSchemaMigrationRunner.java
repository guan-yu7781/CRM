package com.crm.personal.crm.maintenance;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AnnualMaintenanceSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public AnnualMaintenanceSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn("project_id", "alter table annual_maintenance add column project_id bigint");
        ensureColumn("renew_status", "alter table annual_maintenance add column renew_status varchar(32) default 'NOT_RENEWED'");
        normalizeRenewStatus();
    }

    private void normalizeRenewStatus() {
        jdbcTemplate.update(
                "update annual_maintenance set renew_status = case " +
                        "when renew_status is null or trim(renew_status) = '' then 'NOT_RENEWED' " +
                        "when upper(replace(replace(trim(renew_status), '-', '_'), ' ', '_')) in ('RENEWED', 'RENEW') then 'RENEWED' " +
                        "when upper(replace(replace(trim(renew_status), '-', '_'), ' ', '_')) in ('NOT_RENEWED', 'NOTRENEWED', 'NOT_RENEW') then 'NOT_RENEWED' " +
                        "else 'NOT_RENEWED' end"
        );
    }

    private void ensureColumn(String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from information_schema.columns where upper(table_name) = 'ANNUAL_MAINTENANCE' and upper(column_name) = upper(?)",
                    Integer.class,
                    columnName
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute(ddl);
            }
        } catch (Exception ignored) {
        }
    }
}
