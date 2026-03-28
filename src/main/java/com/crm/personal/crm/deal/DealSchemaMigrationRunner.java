package com.crm.personal.crm.deal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DealSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DealSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn("converted_project_id", "alter table deals add column converted_project_id bigint");
        ensureColumn("converted_at", "alter table deals add column converted_at datetime");
        ensureColumn("opportunity_type", "alter table deals add column opportunity_type varchar(32) not null default 'ACQUISITION'");
        // Backfill any rows that were inserted before the column existed (null or empty string)
        try {
            jdbcTemplate.execute(
                "update deals set opportunity_type = 'ACQUISITION' where opportunity_type is null or opportunity_type = ''");
        } catch (Exception ignored) {}
    }

    private void ensureColumn(String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from information_schema.columns where upper(table_name) = 'DEALS' and upper(column_name) = upper(?)",
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
