package com.crm.personal.crm.deal;

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
public interface DealMapper {

    @Select("select d.id, d.title, d.amount, d.stage, d.expected_close_date, d.notes, d.customer_id, " +
            "c.name as customer_name, d.converted_project_id, d.converted_at, d.created_at, d.updated_at " +
            "from deals d join customers c on c.id = d.customer_id " +
            "order by d.updated_at desc, d.id desc")
    @Results(id = "dealRecordMap", value = {
            @Result(property = "expectedCloseDate", column = "expected_close_date"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "convertedProjectId", column = "converted_project_id"),
            @Result(property = "convertedAt", column = "converted_at"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<DealRecord> findAll();

    @Select("select d.id, d.title, d.amount, d.stage, d.expected_close_date, d.notes, d.customer_id, " +
            "c.name as customer_name, d.converted_project_id, d.converted_at, d.created_at, d.updated_at " +
            "from deals d join customers c on c.id = d.customer_id " +
            "order by d.updated_at desc, d.id desc " +
            "limit #{size} offset #{offset}")
    @org.apache.ibatis.annotations.ResultMap("dealRecordMap")
    List<DealRecord> findPaged(@Param("size") int size, @Param("offset") int offset);

    @Select("select d.id, d.title, d.amount, d.stage, d.expected_close_date, d.notes, d.customer_id, " +
            "c.name as customer_name, d.created_at, d.updated_at " +
            "from deals d join customers c on c.id = d.customer_id " +
            "where d.customer_id = #{customerId} " +
            "order by d.updated_at desc, d.id desc")
    @org.apache.ibatis.annotations.ResultMap("dealRecordMap")
    List<DealRecord> findByCustomerId(Long customerId);

    @Select("select d.id, d.title, d.amount, d.stage, d.expected_close_date, d.notes, d.customer_id, " +
            "c.name as customer_name, d.converted_project_id, d.converted_at, d.created_at, d.updated_at " +
            "from deals d join customers c on c.id = d.customer_id " +
            "where d.id = #{id}")
    @org.apache.ibatis.annotations.ResultMap("dealRecordMap")
    DealRecord findById(Long id);

    @Select("select count(1) from deals where converted_project_id = #{projectId}")
    int countByConvertedProjectId(@Param("projectId") Long projectId);

    @Insert("insert into deals " +
            "(title, amount, stage, expected_close_date, notes, customer_id, converted_project_id, converted_at, created_at, updated_at) " +
            "values " +
            "(#{title}, #{amount}, #{stage}, #{expectedCloseDate}, #{notes}, #{customerId}, #{convertedProjectId}, #{convertedAt}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DealRecord record);

    @Update("update deals set " +
            "title = #{title}, " +
            "amount = #{amount}, " +
            "stage = #{stage}, " +
            "expected_close_date = #{expectedCloseDate}, " +
            "notes = #{notes}, " +
            "customer_id = #{customerId}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{id}")
    int update(DealRecord record);

    @Update("update deals set " +
            "converted_project_id = #{projectId}, " +
            "converted_at = #{convertedAt}, " +
            "updated_at = #{updatedAt} " +
            "where id = #{dealId}")
    int updateConversion(@Param("dealId") Long dealId,
                         @Param("projectId") Long projectId,
                         @Param("convertedAt") java.time.LocalDateTime convertedAt,
                         @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Delete("delete from deals where id = #{id}")
    int deleteById(@Param("id") Long id);
}
