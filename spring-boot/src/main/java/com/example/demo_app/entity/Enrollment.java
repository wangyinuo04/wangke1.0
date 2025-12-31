package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "Enrollment", keepGlobalPrefix = false)
public class Enrollment {
    @TableField(value = "student_id")
    private String studentId;

    @TableField(value = "class_id")
    private String classId;

    @TableField(value = "join_time")
    private LocalDateTime joinTime;
}