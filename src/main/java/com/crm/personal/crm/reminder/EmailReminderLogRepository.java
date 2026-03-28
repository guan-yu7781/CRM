package com.crm.personal.crm.reminder;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmailReminderLogRepository {

    @Select("select count(1) from email_reminder_logs " +
            "where maintenance_id = #{maintenanceId} and reminder_type = #{reminderType}")
    int countByMaintenanceIdAndReminderType(@Param("maintenanceId") Long maintenanceId,
                                            @Param("reminderType") String reminderType);

    @Insert("insert into email_reminder_logs " +
            "(maintenance_id, reminder_type, status, recipient_email, error_message, sent_at) " +
            "values " +
            "(#{maintenanceId}, #{reminderType}, #{status}, #{recipientEmail}, #{errorMessage}, #{sentAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EmailReminderLog log);

    @Select("select id, maintenance_id, reminder_type, status, recipient_email, error_message, sent_at " +
            "from email_reminder_logs order by sent_at desc")
    @Results(id = "emailReminderLogMap", value = {
            @Result(property = "maintenanceId",  column = "maintenance_id"),
            @Result(property = "reminderType",   column = "reminder_type"),
            @Result(property = "recipientEmail", column = "recipient_email"),
            @Result(property = "errorMessage",   column = "error_message"),
            @Result(property = "sentAt",         column = "sent_at")
    })
    List<EmailReminderLog> findAllOrderBySentAtDesc();

    @Select("select id, maintenance_id, reminder_type, status, recipient_email, error_message, sent_at " +
            "from email_reminder_logs where maintenance_id = #{maintenanceId} order by sent_at desc")
    @ResultMap("emailReminderLogMap")
    List<EmailReminderLog> findByMaintenanceIdOrderBySentAtDesc(Long maintenanceId);
}
