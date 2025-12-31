package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("PaperQuestion")
public class PaperQuestion {

    private String paperId;              // 试卷ID

    private String questionId;           // 试题ID

    private Integer questionOrder;       // 题目顺序
}