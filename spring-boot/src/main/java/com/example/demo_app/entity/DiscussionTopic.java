package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("DiscussionTopic")
public class DiscussionTopic {
    @TableId(type = IdType.ASSIGN_ID)
    private String topicId;
    private String topicTitle;
    private String topicContent;
    private LocalDateTime publishTime;
    private String classId;
    private String authorId;
    private Integer topOrder = 0;

    // 非数据库字段，使用 @TableField(exist = false) 标记
    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String role;

    @TableField(exist = false)
    private Integer replyCount = 0;

    @TableField(exist = false)
    private Integer viewCount = 0;

    @TableField(exist = false)
    private LocalDateTime lastReplyTime;
}