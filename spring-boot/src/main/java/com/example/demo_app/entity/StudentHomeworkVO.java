package com.example.demo_app.entity;

import lombok.Data;

@Data
public class StudentHomeworkVO {
    // 作业基本信息
    private String id;              // 作业ID
    private String courseName;      // 课程名称
    private String title;           // 作业标题
    private String deadline;        // 截止时间 (字符串格式)
    private Float totalScore;       // 总分
    private String description;     // 作业描述
    private String attachment;      // 作业附件路径

    // 学生提交状态信息
    private String status;             // 状态：未提交/已提交/已逾期/已批改
    private Float score;               // 得分
    private String submitTime;         // 提交时间
    private String submissionContent;  // 提交的文本内容
    private String submissionFile;     // 提交的文件名称
    private String feedback;           // 老师评语
    private String gradeTime;          // 批改时间
}