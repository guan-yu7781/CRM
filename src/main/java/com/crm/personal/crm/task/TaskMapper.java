package com.crm.personal.crm.task;

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
public interface TaskMapper {

    @Select("select t.id, t.title, t.description, t.status, t.priority, t.due_date, t.customer_id, " +
            "c.name as customer_name, t.deal_id, d.title as deal_title, t.created_at, t.updated_at " +
            "from crm_tasks t " +
            "join customers c on c.id = t.customer_id " +
            "left join deals d on d.id = t.deal_id " +
            "order by t.updated_at desc, t.id desc")
    @Results(id = "taskRecordMap", value = {
            @Result(property = "dueDate", column = "due_date"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "dealId", column = "deal_id"),
            @Result(property = "dealTitle", column = "deal_title"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<TaskRecord> findAll();

    @Select("select t.id, t.title, t.description, t.status, t.priority, t.due_date, t.customer_id, " +
            "c.name as customer_name, t.deal_id, d.title as deal_title, t.created_at, t.updated_at " +
            "from crm_tasks t " +
            "join customers c on c.id = t.customer_id " +
            "left join deals d on d.id = t.deal_id " +
            "order by t.updated_at desc, t.id desc " +
            "limit #{size} offset #{offset}")
    @org.apache.ibatis.annotations.ResultMap("taskRecordMap")
    List<TaskRecord> findPaged(@Param("size") int size, @Param("offset") int offset);

    @Select("select t.id, t.title, t.description, t.status, t.priority, t.due_date, t.customer_id, " +
            "c.name as customer_name, t.deal_id, d.title as deal_title, t.created_at, t.updated_at " +
            "from crm_tasks t " +
            "join customers c on c.id = t.customer_id " +
            "left join deals d on d.id = t.deal_id " +
            "where t.customer_id = #{customerId} " +
            "order by t.updated_at desc, t.id desc")
    @org.apache.ibatis.annotations.ResultMap("taskRecordMap")
    List<TaskRecord> findByCustomerId(Long customerId);

    @Select("select t.id, t.title, t.description, t.status, t.priority, t.due_date, t.customer_id, " +
            "c.name as customer_name, t.deal_id, d.title as deal_title, t.created_at, t.updated_at " +
            "from crm_tasks t " +
            "join customers c on c.id = t.customer_id " +
            "left join deals d on d.id = t.deal_id " +
            "where t.status = #{status} " +
            "order by t.updated_at desc, t.id desc")
    @org.apache.ibatis.annotations.ResultMap("taskRecordMap")
    List<TaskRecord> findByStatus(TaskStatus status);

    @Select("select t.id, t.title, t.description, t.status, t.priority, t.due_date, t.customer_id, " +
            "c.name as customer_name, t.deal_id, d.title as deal_title, t.created_at, t.updated_at " +
            "from crm_tasks t " +
            "join customers c on c.id = t.customer_id " +
            "left join deals d on d.id = t.deal_id " +
            "where t.id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("taskRecordMap")
    TaskRecord findById(Long id);

    @Insert("insert into crm_tasks " +
            "(title, description, status, priority, due_date, customer_id, deal_id, created_at, updated_at) " +
            "values " +
            "(#{title}, #{description}, #{status}, #{priority}, #{dueDate}, #{customerId}, #{dealId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskRecord record);

    @Update("update crm_tasks set " +
            "title = #{title}, " +
            "description = #{description}, " +
            "status = #{status}, " +
            "priority = #{priority}, " +
            "due_date = #{dueDate}, " +
            "customer_id = #{customerId}, " +
            "deal_id = #{dealId}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(TaskRecord record);

    @Delete("delete from crm_tasks where id = #{id}")
    int deleteById(@Param("id") Long id);
}
