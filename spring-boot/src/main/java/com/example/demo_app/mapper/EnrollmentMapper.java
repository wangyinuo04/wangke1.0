package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    /**
     * 根据class_id查询学生ID列表
     */
    @Select("SELECT student_id FROM Enrollment WHERE class_id = #{classId}")
    List<String> findStudentIdsByClassId(String classId);

    /**
     * 根据student_id查询教学班ID列表
     */
    @Select("SELECT class_id FROM Enrollment WHERE student_id = #{studentId}")
    List<String> findClassIdsByStudentId(String studentId);

    /**
     * 根据class_id统计学生数量
     */
    @Select("SELECT COUNT(*) FROM Enrollment WHERE class_id = #{classId}")
    long countByClassId(String classId);
}