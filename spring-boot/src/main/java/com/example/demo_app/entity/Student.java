package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "Student", keepGlobalPrefix = false)
public class Student {
    @TableId(value = "student_id", type = IdType.INPUT)
    private String studentId;

    private String name;
    private String gender;
    private String department;

    @TableField(value = "class")
    private String className;

    @TableField(value = "login_password")
    private String loginPassword;

    private String phone;
    private String email;

    @TableField(value = "account_status")
    private String accountStatus;

    private String major;

    @TableField(value = "enrollment_year")
    private Integer enrollmentYear;

    @TableField(value = "avatar_path")
    private String avatarPath;

    @TableField(value = "admin_account")
    private String adminAccount;
}