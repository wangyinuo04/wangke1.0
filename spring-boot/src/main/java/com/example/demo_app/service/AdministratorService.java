package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Administrator;
import com.example.demo_app.mapper.AdministratorMapper;
import org.springframework.stereotype.Service;

@Service
public class AdministratorService extends ServiceImpl<AdministratorMapper, Administrator> {

    /**
     * 验证管理员密码
     */
    public boolean verifyPassword(String account, String password) {
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        Administrator admin = getOne(queryWrapper);

        if (admin == null) {
            return false;
        }

        // 目前是明文密码存储，直接比较
        return admin.getLoginPassword().equals(password);
    }

    /**
     * 修改管理员密码
     */
    public boolean updatePassword(String account, String newPassword) {
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        Administrator admin = getOne(queryWrapper);

        if (admin == null) {
            return false;
        }

        admin.setLoginPassword(newPassword);
        return updateById(admin);
    }

    /**
     * 根据账号获取管理员信息
     */
    public Administrator getByAccount(String account) {
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        return getOne(queryWrapper);
    }
}