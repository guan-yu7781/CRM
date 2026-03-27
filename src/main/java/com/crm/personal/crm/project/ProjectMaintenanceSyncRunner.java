package com.crm.personal.crm.project;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMaintenanceSyncRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;

    public ProjectMaintenanceSyncRunner(JdbcTemplate jdbcTemplate, ProjectService projectService) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureProjectIdColumn();

        List<MaintenanceStub> records = jdbcTemplate.query(
                "select id, customer_id, project_name, market from annual_maintenance where project_id is null",
                (rs, rowNum) -> new MaintenanceStub(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getString("project_name"),
                        rs.getString("market")
                )
        );

        for (MaintenanceStub record : records) {
            ProjectRecord project = projectService.findOrCreateProjectForMaintenance(
                    record.getCustomerId(),
                    record.getProjectName(),
                    record.getMarket()
            );
            jdbcTemplate.update(
                    "update annual_maintenance set project_id = ? where id = ?",
                    project.getId(),
                    record.getId()
            );
        }
    }

    private void ensureProjectIdColumn() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from information_schema.columns " +
                            "where upper(table_name) = 'ANNUAL_MAINTENANCE' and upper(column_name) = 'PROJECT_ID'",
                    Integer.class
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("alter table annual_maintenance add column project_id bigint");
            }
        } catch (Exception ignored) {
        }
    }

    private static class MaintenanceStub {
        private final Long id;
        private final Long customerId;
        private final String projectName;
        private final String market;

        private MaintenanceStub(Long id, Long customerId, String projectName, String market) {
            this.id = id;
            this.customerId = customerId;
            this.projectName = projectName;
            this.market = market;
        }

        public Long getId() {
            return id;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getMarket() {
            return market;
        }
    }
}
