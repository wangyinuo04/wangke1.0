package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("HomeworkSubmission")
public class HomeworkSubmission {
    @TableId(value = "submission_id", type = IdType.INPUT)
    private String submissionId;

    private String homeworkId;

    private String studentId;

    private String submissionText;

    private String attachmentPath;

    private LocalDateTime submissionTime;

    private String submissionStatus;

    private Float score;

    private String gradingStatus;

    private String teacherFeedback;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;
}