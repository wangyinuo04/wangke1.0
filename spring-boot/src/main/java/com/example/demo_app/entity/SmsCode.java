package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "SMS_Code")
public class SmsCode {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String contact;  // 手机号或邮箱
    private String code;     // 验证码
    private String type;     // 类型
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Boolean used;    // 是否已使用
}