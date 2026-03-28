package com.crm.personal.crm.reminder;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MaintenanceExpiryMapper {

    String SELECT_FIELDS =
            "select am.id, am.project_name, am.market, am.maintenance_year, am.end_date, " +
            "am.amount, am.payment_status, am.customer_id, c.name as customer_name " +
            "from annual_maintenance am " +
            "join customers c on c.id = am.customer_id ";

    String RESULT_MAPPING_ID = "maintenanceExpiryRecordMap";

    @Select(SELECT_FIELDS + "where am.end_date = #{targetDate}")
    @Results(id = RESULT_MAPPING_ID, value = {
            @Result(property = "projectName",    column = "project_name"),
            @Result(property = "maintenanceYear", column = "maintenance_year"),
            @Result(property = "endDate",        column = "end_date"),
            @Result(property = "paymentStatus",  column = "payment_status"),
            @Result(property = "customerId",     column = "customer_id"),
            @Result(property = "customerName",   column = "customer_name")
    })
    List<MaintenanceExpiryRecord> findByEndDate(@Param("targetDate") LocalDate targetDate);

    /**
     * Used for EXPIRED reminders: covers both expiry day and 1-day-overdue
     * in case the scheduler was down on expiry day.
     */
    @Select(SELECT_FIELDS + "where am.end_date >= #{fromDate} and am.end_date <= #{toDate}")
    @Results(id = "maintenanceExpiryRangeMap", value = {
            @Result(property = "projectName",    column = "project_name"),
            @Result(property = "maintenanceYear", column = "maintenance_year"),
            @Result(property = "endDate",        column = "end_date"),
            @Result(property = "paymentStatus",  column = "payment_status"),
            @Result(property = "customerId",     column = "customer_id"),
            @Result(property = "customerName",   column = "customer_name")
    })
    List<MaintenanceExpiryRecord> findByEndDateBetween(@Param("fromDate") LocalDate fromDate,
                                                       @Param("toDate") LocalDate toDate);
}
