package com.crm.personal.crm.contact;

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
public interface ContactMapper {

    @Select("select ct.id, ct.first_name, ct.last_name, ct.email, ct.phone, ct.job_title, ct.notes, " +
            "ct.customer_id, c.name as customer_name, ct.created_at, ct.updated_at " +
            "from contacts ct join customers c on c.id = ct.customer_id " +
            "order by ct.updated_at desc, ct.id desc")
    @Results(id = "contactRecordMap", value = {
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "jobTitle", column = "job_title"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<ContactRecord> findAll();

    @Select("select ct.id, ct.first_name, ct.last_name, ct.email, ct.phone, ct.job_title, ct.notes, " +
            "ct.customer_id, c.name as customer_name, ct.created_at, ct.updated_at " +
            "from contacts ct join customers c on c.id = ct.customer_id " +
            "order by ct.updated_at desc, ct.id desc " +
            "limit #{size} offset #{offset}")
    @org.apache.ibatis.annotations.ResultMap("contactRecordMap")
    List<ContactRecord> findPaged(@Param("size") int size, @Param("offset") int offset);

    @Select("select ct.id, ct.first_name, ct.last_name, ct.email, ct.phone, ct.job_title, ct.notes, " +
            "ct.customer_id, c.name as customer_name, ct.created_at, ct.updated_at " +
            "from contacts ct join customers c on c.id = ct.customer_id " +
            "where ct.customer_id = #{customerId} " +
            "order by ct.updated_at desc, ct.id desc")
    @org.apache.ibatis.annotations.ResultMap("contactRecordMap")
    List<ContactRecord> findByCustomerId(Long customerId);

    @Select("select ct.id, ct.first_name, ct.last_name, ct.email, ct.phone, ct.job_title, ct.notes, " +
            "ct.customer_id, c.name as customer_name, ct.created_at, ct.updated_at " +
            "from contacts ct join customers c on c.id = ct.customer_id " +
            "where ct.id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("contactRecordMap")
    ContactRecord findById(Long id);

    @Insert("insert into contacts " +
            "(first_name, last_name, email, phone, job_title, notes, customer_id, created_at, updated_at) " +
            "values " +
            "(#{firstName}, #{lastName}, #{email}, #{phone}, #{jobTitle}, #{notes}, #{customerId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ContactRecord record);

    @Update("update contacts set " +
            "first_name = #{firstName}, " +
            "last_name = #{lastName}, " +
            "email = #{email}, " +
            "phone = #{phone}, " +
            "job_title = #{jobTitle}, " +
            "notes = #{notes}, " +
            "customer_id = #{customerId}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(ContactRecord record);

    @Delete("delete from contacts where id = #{id}")
    int deleteById(@Param("id") Long id);
}
