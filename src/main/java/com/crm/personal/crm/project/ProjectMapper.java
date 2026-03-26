package com.crm.personal.crm.project;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProjectMapper {

    @Select("select p.id, p.project_name, p.market, p.amount, p.tax_rate, p.status, p.customer_id, " +
            "c.name as customer_name, p.created_at, p.updated_at " +
            "from projects p join customers c on c.id = p.customer_id " +
            "order by p.updated_at desc, p.id desc")
    @Results(id = "projectRecordMap", value = {
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "taxRate", column = "tax_rate"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<ProjectRecord> findAll();

    @Select("select p.id, p.project_name, p.market, p.amount, p.tax_rate, p.status, p.customer_id, " +
            "c.name as customer_name, p.created_at, p.updated_at " +
            "from projects p join customers c on c.id = p.customer_id " +
            "where p.customer_id = #{customerId} " +
            "order by p.project_name asc")
    @org.apache.ibatis.annotations.ResultMap("projectRecordMap")
    List<ProjectRecord> findByCustomerId(Long customerId);

    @Select("select p.id, p.project_name, p.market, p.amount, p.tax_rate, p.status, p.customer_id, " +
            "c.name as customer_name, p.created_at, p.updated_at " +
            "from projects p join customers c on c.id = p.customer_id " +
            "where p.id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("projectRecordMap")
    ProjectRecord findById(Long id);

    @Select("select p.id, p.project_name, p.market, p.amount, p.tax_rate, p.status, p.customer_id, " +
            "c.name as customer_name, p.created_at, p.updated_at " +
            "from projects p join customers c on c.id = p.customer_id " +
            "where p.customer_id = #{customerId} and lower(p.project_name) = lower(#{projectName}) limit 1")
    @org.apache.ibatis.annotations.ResultMap("projectRecordMap")
    ProjectRecord findByCustomerIdAndProjectName(@Param("customerId") Long customerId,
                                                 @Param("projectName") String projectName);

    @Insert("insert into projects " +
            "(project_name, market, amount, tax_rate, status, customer_id, created_at, updated_at) " +
            "values " +
            "(#{projectName}, #{market}, #{amount}, #{taxRate}, #{status}, #{customerId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectRecord record);

    @Update("update projects set " +
            "project_name = #{projectName}, " +
            "market = #{market}, " +
            "amount = #{amount}, " +
            "tax_rate = #{taxRate}, " +
            "status = #{status}, " +
            "customer_id = #{customerId}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(ProjectRecord record);

    @Delete("delete from projects where id = #{id}")
    int deleteById(@Param("id") Long id);
}
