package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {

    @Select("SELECT p.* FROM ExamPaper p " +
            "WHERE p.course_id IN (SELECT DISTINCT tc.course_id FROM TeachingClass tc WHERE tc.teacher_id = #{teacherId}) " +
            "ORDER BY p.created_time DESC")
    List<ExamPaper> getPapersByTeacher(@Param("teacherId") String teacherId);

    @Select("SELECT p.* FROM ExamPaper p " +
            "WHERE p.paper_status = '已发布' AND p.course_id = #{courseId} " +
            "ORDER BY p.created_time DESC")
    List<ExamPaper> getPublishedPapersByCourse(@Param("courseId") String courseId);

    // 新增：按课程分组获取试卷
    @Select("SELECT p.*, c.course_name FROM ExamPaper p " +
            "LEFT JOIN Course c ON p.course_id = c.course_id " +
            "WHERE p.course_id IN (SELECT DISTINCT tc.course_id FROM TeachingClass tc WHERE tc.teacher_id = #{teacherId}) " +
            "ORDER BY c.course_name, p.created_time DESC")
    List<Map<String, Object>> getPapersGroupedByCourse(@Param("teacherId") String teacherId);
}