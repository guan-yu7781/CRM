package com.crm.personal.crm.project;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ProjectSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ProjectSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn(
                "implementation_amount",
                "alter table projects add column implementation_amount decimal(14,2) not null default 0.00"
        );
        ensureColumn(
                "source_deal_id",
                "alter table projects add column source_deal_id bigint"
        );
        jdbcTemplate.update("update projects set implementation_amount = 0.00 where implementation_amount is null");
        normalizeProjectStatus();
    }

    private void normalizeProjectStatus() {
        jdbcTemplate.update(
                "update projects set status = case " +
                        "when status is null or trim(status) = '' then 'UNSIGNED_CONTRACT' " +
                        "when upper(replace(replace(trim(status), '-', '_'), ' ', '_')) in ('LIVE', 'SIGNED_CONTRACT', 'SIGNED') then 'SIGNED_CONTRACT' " +
                        "when upper(replace(replace(trim(status), '-', '_'), ' ', '_')) in ('IN_PROGRESS', 'UNSIGNED_CONTRACT', 'UNSIGNED') then 'UNSIGNED_CONTRACT' " +
                        "else 'UNSIGNED_CONTRACT' end"
        );
    }

    private void ensureColumn(String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from information_schema.columns where table_schema = database() and table_name = 'projects' and column_name = ?",
                Integer.class,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }
}
