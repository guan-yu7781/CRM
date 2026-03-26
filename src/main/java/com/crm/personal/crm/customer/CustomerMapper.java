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

    @Select("select id, name, customer_type, cif_number, email, phone, company, segment, status, " +
            "kyc_status, risk_level, preferred_channel, onboarding_stage, residency_country, " +
            "relationship_manager, notes, created_at, updated_at " +
            "from customers order by updated_at desc, id desc")
    @Results(id = "customerRecordMap", value = {
            @Result(property = "customerType", column = "customer_type"),
            @Result(property = "cifNumber", column = "cif_number"),
            @Result(property = "kycStatus", column = "kyc_status"),
            @Result(property = "riskLevel", column = "risk_level"),
            @Result(property = "preferredChannel", column = "preferred_channel"),
            @Result(property = "onboardingStage", column = "onboarding_stage"),
            @Result(property = "residencyCountry", column = "residency_country"),
            @Result(property = "relationshipManager", column = "relationship_manager"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CustomerRecord> findAll();

    @Select("select id, name, customer_type, cif_number, email, phone, company, segment, status, " +
            "kyc_status, risk_level, preferred_channel, onboarding_stage, residency_country, " +
            "relationship_manager, notes, created_at, updated_at " +
            "from customers where id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findById(Long id);

    @Select("select id, name, customer_type, cif_number, email, phone, company, segment, status, " +
            "kyc_status, risk_level, preferred_channel, onboarding_stage, residency_country, " +
            "relationship_manager, notes, created_at, updated_at " +
            "from customers where lower(email) = lower(#{email}) limit 1")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findByEmail(String email);

    @Select("select id, name, customer_type, cif_number, email, phone, company, segment, status, " +
            "kyc_status, risk_level, preferred_channel, onboarding_stage, residency_country, " +
            "relationship_manager, notes, created_at, updated_at " +
            "from customers where lower(cif_number) = lower(#{cifNumber}) limit 1")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findByCifNumberIgnoreCase(String cifNumber);

    @Select("select id, name, customer_type, cif_number, email, phone, company, segment, status, " +
            "kyc_status, risk_level, preferred_channel, onboarding_stage, residency_country, " +
            "relationship_manager, notes, created_at, updated_at " +
            "from customers order by id asc limit 1")
    @org.apache.ibatis.annotations.ResultMap("customerRecordMap")
    CustomerRecord findFirst();

    @Select("select count(1) from customers where id = #{id}")
    int countById(Long id);

    @Insert("insert into customers " +
            "(name, customer_type, cif_number, email, phone, company, segment, status, kyc_status, " +
            "risk_level, preferred_channel, onboarding_stage, residency_country, relationship_manager, " +
            "notes, created_at, updated_at) " +
            "values " +
            "(#{name}, #{customerType}, #{cifNumber}, #{email}, #{phone}, #{company}, #{segment}, #{status}, " +
            "#{kycStatus}, #{riskLevel}, #{preferredChannel}, #{onboardingStage}, #{residencyCountry}, " +
            "#{relationshipManager}, #{notes}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerRecord record);

    @Update("update customers set " +
            "name = #{name}, " +
            "customer_type = #{customerType}, " +
            "cif_number = #{cifNumber}, " +
            "email = #{email}, " +
            "phone = #{phone}, " +
            "company = #{company}, " +
            "segment = #{segment}, " +
            "status = #{status}, " +
            "kyc_status = #{kycStatus}, " +
            "risk_level = #{riskLevel}, " +
            "preferred_channel = #{preferredChannel}, " +
            "onboarding_stage = #{onboardingStage}, " +
            "residency_country = #{residencyCountry}, " +
            "relationship_manager = #{relationshipManager}, " +
            "notes = #{notes}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(CustomerRecord record);

    @Delete("delete from customers where id = #{id}")
    int deleteById(@Param("id") Long id);
}
