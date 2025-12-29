package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Administrator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdministratorMapper extends BaseMapper<Administrator> {
}