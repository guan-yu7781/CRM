package com.crm.personal.crm.maintenance;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AnnualMaintenanceMapper {

    String NORMALIZED_RENEW_STATUS =
            "case " +
                    "when am.renew_status is null or trim(am.renew_status) = '' then 'NOT_RENEWED' " +
                    "when upper(replace(replace(trim(am.renew_status), '-', '_'), ' ', '_')) in ('RENEWED', 'RENEW') then 'RENEWED' " +
                    "when upper(replace(replace(trim(am.renew_status), '-', '_'), ' ', '_')) in ('NOT_RENEWED', 'NOTRENEWED', 'NOT_RENEW') then 'NOT_RENEWED' " +
                    "else 'NOT_RENEWED' end as renew_status";

    @Select("select " +
            "am.id, " +
            "am.project_id, " +
            "am.project_name, " +
            "am.market, " +
            "am.maintenance_year, " +
            "am.amount, " +
            "am.start_date, " +
            "am.end_date, " +
            "am.payment_status, " +
            NORMALIZED_RENEW_STATUS + ", " +
            "am.customer_id, " +
            "c.name as customer_name, " +
            "am.created_at, " +
            "am.updated_at " +
            "from annual_maintenance am " +
            "join projects p on p.id = am.project_id " +
            "join customers c on c.id = am.customer_id " +
            "where am.customer_id = #{customerId} " +
            "order by am.project_name asc, am.maintenance_year asc")
    @Results(id = "annualMaintenanceRecordMap", value = {
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "maintenanceYear", column = "maintenance_year"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "paymentStatus", column = "payment_status"),
            @Result(property = "renewStatus", column = "renew_status"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<AnnualMaintenanceRecord> findByCustomerId(Long customerId);

    @Select("select " +
            "am.id, " +
            "am.project_id, " +
            "am.project_name, " +
            "am.market, " +
            "am.maintenance_year, " +
            "am.amount, " +
            "am.start_date, " +
            "am.end_date, " +
            "am.payment_status, " +
            NORMALIZED_RENEW_STATUS + ", " +
            "am.customer_id, " +
            "c.name as customer_name, " +
            "am.created_at, " +
            "am.updated_at " +
            "from annual_maintenance am " +
            "join projects p on p.id = am.project_id " +
            "join customers c on c.id = am.customer_id " +
            "where am.id = #{id}")
    @ResultMap("annualMaintenanceRecordMap")
    AnnualMaintenanceRecord findById(Long id);

    @Select("select count(1) " +
            "from annual_maintenance " +
            "where customer_id = #{customerId} " +
            "and project_id = #{projectId} " +
            "and maintenance_year = #{maintenanceYear}")
    int countByCustomerIdAndProjectIdAndMaintenanceYear(@Param("customerId") Long customerId,
                                                          @Param("projectId") Long projectId,
                                                          @Param("maintenanceYear") Integer maintenanceYear);

    @Select("select " +
            "am.id, " +
            "am.project_id, " +
            "am.project_name, " +
            "am.market, " +
            "am.maintenance_year, " +
            "am.amount, " +
            "am.start_date, " +
            "am.end_date, " +
            "am.payment_status, " +
            NORMALIZED_RENEW_STATUS + ", " +
            "am.customer_id, " +
            "c.name as customer_name, " +
            "am.created_at, " +
            "am.updated_at " +
            "from annual_maintenance am " +
            "join customers c on c.id = am.customer_id " +
            "where am.customer_id = #{customerId} " +
            "and am.project_id = #{projectId} " +
            "and am.maintenance_year = #{maintenanceYear} " +
            "limit 1")
    @ResultMap("annualMaintenanceRecordMap")
    AnnualMaintenanceRecord findByCustomerIdAndProjectIdAndMaintenanceYear(@Param("customerId") Long customerId,
                                                                           @Param("projectId") Long projectId,
                                                                           @Param("maintenanceYear") Integer maintenanceYear);

    @Select("select count(1) " +
            "from annual_maintenance " +
            "where customer_id = #{customerId} " +
            "and project_id = #{projectId} " +
            "and maintenance_year = #{maintenanceYear} " +
            "and id <> #{excludedId}")
    int countConflictsExcludingId(@Param("customerId") Long customerId,
                                  @Param("projectId") Long projectId,
                                  @Param("maintenanceYear") Integer maintenanceYear,
                                  @Param("excludedId") Long excludedId);

    @Insert("insert into annual_maintenance " +
            "(project_id, project_name, market, maintenance_year, amount, start_date, end_date, payment_status, renew_status, customer_id, created_at, updated_at) " +
            "values " +
            "(#{projectId}, #{projectName}, #{market}, #{maintenanceYear}, #{amount}, #{startDate}, #{endDate}, #{paymentStatus}, #{renewStatus}, #{customerId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AnnualMaintenanceRecord record);

    @Update("update annual_maintenance set " +
            "project_id = #{projectId}, " +
            "project_name = #{projectName}, " +
            "market = #{market}, " +
            "maintenance_year = #{maintenanceYear}, " +
            "amount = #{amount}, " +
            "start_date = #{startDate}, " +
            "end_date = #{endDate}, " +
            "payment_status = #{paymentStatus}, " +
            "renew_status = #{renewStatus}, " +
            "customer_id = #{customerId}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(AnnualMaintenanceRecord record);
}
