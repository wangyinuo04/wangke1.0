package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    @Select("SELECT e.* FROM Exam e " +
            "LEFT JOIN TeachingClass tc ON e.class_id = tc.class_id " +
            "WHERE tc.teacher_id = #{teacherId} " +
            "ORDER BY e.start_time DESC")
    List<Exam> getExamsByTeacher(@Param("teacherId") String teacherId);

    @Select("SELECT e.* FROM Exam e " +
            "WHERE e.class_id = #{classId} " +
            "ORDER BY e.start_time DESC")
    List<Exam> getExamsByClass(@Param("classId") String classId);
}