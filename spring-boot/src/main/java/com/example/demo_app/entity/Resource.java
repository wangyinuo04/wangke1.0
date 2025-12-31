package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Resource")
public class Resource {
    @TableId(value = "resource_id", type = IdType.ASSIGN_UUID)
    private String resourceId;

    private String resourceName;
    private String resourceType; // 视频, PDF, PPT, Word, 音频
    private String filePath;
    private Long fileSize;
    private Boolean allowDownload;
    private LocalDateTime uploadTime;
    private String chapterId;

    // 临时字段，用于前端展示，不在数据库中存在
    @TableField(exist = false)
    private String chapterName;

    @TableField(exist = false)
    private String formattedSize; // 格式化的文件大小

    @TableField(exist = false)
    private String formattedUploadTime; // 格式化的上传时间
}