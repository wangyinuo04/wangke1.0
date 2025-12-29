package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "TeachingClass", keepGlobalPrefix = false)
public class TeachingClass {
    @TableId(value = "class_id", type = IdType.INPUT)
    private String classId;

    @TableField(value = "semester")
    private String semester;

    @TableField(value = "invitation_code")
    private String invitationCode;

    @TableField(value = "expiry_date")
    private LocalDateTime expiryDate;

    @TableField(value = "class_name")
    private String className;

    @TableField(value = "max_students")
    private Integer maxStudents;

    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    @TableField(value = "admin_account")
    private String adminAccount;

    @TableField(value = "teacher_id")
    private String teacherId;

    @TableField(value = "course_id")
    private String courseId;
}