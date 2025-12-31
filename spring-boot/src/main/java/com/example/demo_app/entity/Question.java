package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("Question")
public class Question {

    @TableId
    private String questionId;           // 试题ID

    private String content;              // 题干内容

    private String questionType;         // 题目类型: 单选/多选/判断/简答

    private String options;              // 选项内容

    private String correctAnswer;        // 正确答案

    private String explanation;          // 题目解析

    private String difficulty;           // 难度: 低/中/高

    private Float score;                 // 分值

    private String courseId;             // 所属课程ID

}