package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.TeachingClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeachingClassMapper extends BaseMapper<TeachingClass> {

    /**
     * 根据class_id查询最大人数
     */
    @Select("SELECT max_students FROM TeachingClass WHERE class_id = #{classId}")
    Integer selectMaxStudentsByClassId(String classId);
}