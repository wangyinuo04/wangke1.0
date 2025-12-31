package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ExamParticipation")
public class ExamParticipation {

    // 复合主键第一部分：student_id
    @TableId(value = "student_id", type = IdType.INPUT)
    private String studentId;

    // 复合主键第二部分：exam_id
    @TableField("exam_id")
    private String examId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("time_used")
    private Integer timeUsed;

    @TableField("total_score")
    private Float totalScore;

    @TableField("objective_score")
    private Float objectiveScore;

    @TableField("subjective_score")
    private Float subjectiveScore;

    @TableField("exam_status")
    private String examStatus;

    @TableField("objective_answers")
    private String objectiveAnswers; // JSON格式字符串

    @TableField("subjective_answers")
    private String subjectiveAnswers; // JSON格式字符串

    @TableField("answer_snapshot")
    private String answerSnapshot; // JSON格式字符串

    // 关联的学生信息（非数据库字段）
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String studentAvatar;
}