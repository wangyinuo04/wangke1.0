package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("DiscussionReply")
public class DiscussionReply {
    @TableId(type = IdType.ASSIGN_ID)
    private String replyId;
    private String replyContent;
    private LocalDateTime publishTime;
    private String topicId;
    private String authorId;

    // 新增的数据库字段（不再是 @TableField(exist = false)）
    private String quoteAuthorId;  // 数据库真实字段
    private String quoteContent;   // 数据库真实字段

    // 非数据库字段（用于显示）
    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String role;

    @TableField(exist = false)
    private String quoteAuthorName; // 引用的作者姓名（不是数据库字段）
}