package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    // 继承BaseMapper后自动有selectBatchIds方法

    @Select("SELECT q.* FROM Question q " +
            "WHERE q.course_id IN (SELECT course_id FROM TeachingClass WHERE teacher_id = #{teacherId}) " +
            "AND (#{keyword} IS NULL OR q.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY q.question_id DESC")
    List<Question> getQuestionsByTeacher(@Param("teacherId") String teacherId,
                                         @Param("keyword") String keyword);

    // 添加按课程ID查询的方法
    @Select("SELECT * FROM Question WHERE course_id = #{courseId}")
    List<Question> getQuestionsByCourse(@Param("courseId") String courseId);
}