package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Chapter")
public class Chapter {
    @TableId(value = "chapter_id", type = IdType.ASSIGN_UUID)
    private String chapterId;

    private String chapterName;

    @TableField("parent_chapter_id")
    private String parentChapterId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("class_id")
    private String classId;

    // 根据数据库字段名修正
    @TableField("created_time")  // 注意数据库中是created_time不是create_time
    private LocalDateTime createdTime;

    // 临时字段，用于前端展示
    @TableField(exist = false)  // 表示这个字段不在数据库表中
    private String parentChapterName;
}