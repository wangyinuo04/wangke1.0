package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ExamPaper")
public class ExamPaper {

    @TableId
    private String paperId;              // 试卷ID

    private String paperTitle;           // 试卷标题

    private Float totalScore;            // 试卷总分

    private String paperStatus;          // 试卷状态: 草稿/已发布

    private String courseId;             // 所属课程ID

    private LocalDateTime createdTime;   // 创建时间

}