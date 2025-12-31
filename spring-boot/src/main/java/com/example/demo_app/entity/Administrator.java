package com.example.demo_app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "Administrator", keepGlobalPrefix = false)
public class Administrator {
    @TableId(value = "account", type = IdType.INPUT)
    private String account;

    @com.baomidou.mybatisplus.annotation.TableField(value = "login_password")
    private String loginPassword;
}