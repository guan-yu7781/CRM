package com.crm.personal.crm.customer;

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
public interface CustomerMapper {

    @Select("select id, name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at " +
            "from customers order by updated_at desc, id desc")
    @Results(id = "customerRecordMap", value = {
            @Result(property = "customerType", column = "customer_type"),
            @Result(property = "cifNumber", column = "cif_number"),
            @Result(property = "riskLevel", column = "risk_level"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CustomerRecord> findAll();

    @Select("select id, name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at " +
            "from customers order by updated_at desc, id desc " +
            "limit #{size} offset #{offset}")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    List<CustomerRecord> findPaged(@Param("size") int size, @Param("offset") int offset);

    @Select("select id, name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at " +
            "from customers where id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findById(Long id);

    @Select("select id, name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at " +
            "from customers where lower(cif_number) = lower(#{cifNumber}) limit 1")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findByCifNumberIgnoreCase(String cifNumber);

    @Select("select id, name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at " +
            "from customers order by id asc limit 1")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findFirst();

    @Select("select count(1) from customers where id = #{id}")
    int countById(Long id);

    @Insert("insert into customers " +
            "(name, customer_type, cif_number, segment, status, " +
            "risk_level, notes, created_at, updated_at) " +
            "values " +
            "(#{name}, #{customerType}, #{cifNumber}, #{segment}, #{status}, " +
            "#{riskLevel}, #{notes}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerRecord record);

    @Update("update customers set " +
            "name = #{name}, " +
            "customer_type = #{customerType}, " +
            "cif_number = #{cifNumber}, " +
            "segment = #{segment}, " +
            "status = #{status}, " +
            "risk_level = #{riskLevel}, " +
            "notes = #{notes}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(CustomerRecord record);

    @Delete("delete from customers where id = #{id}")
    int deleteById(@Param("id") Long id);
}
