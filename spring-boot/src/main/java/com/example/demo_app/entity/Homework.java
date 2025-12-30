package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("Homework")
public class Homework {
    @TableId(value = "homework_id", type = IdType.INPUT)
    private String homeworkId;

    private String homeworkTitle;

    private String description;

    private String attachmentPath;

    private LocalDateTime publishTime;

    private LocalDateTime deadline;

    private Float totalScore;

    private String classId;

    // 临时字段，不存数据库
    @TableField(exist = false)
    private Integer submittedCount;

    @TableField(exist = false)
    private Integer totalCount;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String status;
}