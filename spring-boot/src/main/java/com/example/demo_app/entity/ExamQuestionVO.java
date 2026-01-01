package com.example.demo_app.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ExamQuestionVO {
    private String id;              // 题目ID
    private String type;            // 类型：单选/多选/判断/简答
    private String stem;            // 题干
    private Float score;            // 分值
    private List<Map<String, Object>> options; // 选项列表 [{"key":"A", "val":"内容"}]

    // 注意：这里绝对不能包含 correctAnswer 字段！
}