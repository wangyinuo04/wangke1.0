package com.example.demo_app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private String studentId;
    private String name;
    private String gender;
    private String department;
    private String className; // 使用className避免与关键字冲突
    private String loginPassword;
    private String phone;
    private String email;
    private String accountStatus;
    private String major;
    private Integer enrollmentYear;
    private String avatarPath;
    private String adminAccount;
    private LocalDateTime createdTime;
}