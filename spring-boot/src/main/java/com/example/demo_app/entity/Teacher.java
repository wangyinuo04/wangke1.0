package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "Teacher", keepGlobalPrefix = false)
public class Teacher {
    @TableId(value = "teacher_id", type = IdType.INPUT)
    private String teacherId;

    private String name;
    private String gender;
    private String department;
    private String title;
    private String phone;
    private String email;

    @TableField(value = "login_password")
    private String loginPassword;

    @TableField(value = "account_status")
    private String accountStatus;

    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    @TableField(value = "admin_account")
    private String adminAccount;
}