package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "Course", keepGlobalPrefix = false)
public class Course {
    @TableId(value = "course_id", type = IdType.INPUT)
    private String courseId;

    @TableField(value = "course_name")
    private String courseName;

    private Float credit;

    private String college;

    @TableField(value = "course_type")
    private String courseType;

    private String description;

    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    @TableField(value = "admin_account")
    private String adminAccount;
}