package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("Exam")
public class Exam {

    @TableId
    private String examId;               // 考试ID

    private String examName;             // 考试名称

    private LocalDateTime startTime;     // 开始时间

    private LocalDateTime endTime;       // 结束时间

    private Integer timeLimit;           // 限时时长(分钟)

    private Boolean showAnswers;         // 是否显示答案

    private String classId;              // 教学班ID

    private String paperId;              // 试卷ID

    private LocalDateTime createdTime;   // 创建时间
}