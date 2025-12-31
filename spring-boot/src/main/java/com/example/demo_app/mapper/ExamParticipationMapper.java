package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.ExamParticipation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ExamParticipationMapper extends BaseMapper<ExamParticipation> {

    /**
     * 获取考试的所有参与记录（包含学生信息）
     */
    @Select("SELECT ep.*, s.name as student_name, s.class as class_name, s.avatar_path as student_avatar " +
            "FROM ExamParticipation ep " +
            "LEFT JOIN Student s ON ep.student_id = s.student_id " +
            "WHERE ep.exam_id = #{examId} " +
            "ORDER BY s.name")
    List<ExamParticipation> getExamSubmissions(@Param("examId") String examId);

    /**
     * 获取需要批改的主观题参与记录
     */
    @Select("SELECT ep.*, s.name as student_name, s.class as class_name, s.avatar_path as student_avatar " +
            "FROM ExamParticipation ep " +
            "LEFT JOIN Student s ON ep.student_id = s.student_id " +
            "WHERE ep.exam_id = #{examId} " +
            "AND (ep.subjective_score IS NULL OR ep.exam_status = '已提交') " +
            "AND ep.subjective_answers IS NOT NULL " +
            "AND ep.subjective_answers != '[]' " +
            "ORDER BY ep.submit_time DESC")
    List<ExamParticipation> getPendingGrading(@Param("examId") String examId);

    /**
     * 根据教师ID获取考试参与记录
     */
    @Select("SELECT ep.*, s.name as student_name, s.class as class_name, s.avatar_path as student_avatar " +
            "FROM ExamParticipation ep " +
            "LEFT JOIN Student s ON ep.student_id = s.student_id " +
            "LEFT JOIN Exam e ON ep.exam_id = e.exam_id " +
            "LEFT JOIN TeachingClass tc ON e.class_id = tc.class_id " +
            "WHERE tc.teacher_id = #{teacherId} " +
            "AND e.exam_id = #{examId} " +
            "ORDER BY s.name")
    List<ExamParticipation> getTeacherExamSubmissions(
            @Param("teacherId") String teacherId,
            @Param("examId") String examId);

    /**
     * 更新主观题分数（复合主键需要特殊处理）
     */
    @Update("UPDATE ExamParticipation SET " +
            "subjective_score = #{subjectiveScore}, " +
            "total_score = #{objectiveScore} + #{subjectiveScore}, " +
            "exam_status = '已批改' " +
            "WHERE student_id = #{studentId} AND exam_id = #{examId}")
    int updateSubjectiveScore(
            @Param("studentId") String studentId,
            @Param("examId") String examId,
            @Param("subjectiveScore") Float subjectiveScore,
            @Param("objectiveScore") Float objectiveScore);

    /**
     * 根据复合主键查询记录
     */
    @Select("SELECT * FROM ExamParticipation WHERE student_id = #{studentId} AND exam_id = #{examId}")
    ExamParticipation getByCompositeKey(
            @Param("studentId") String studentId,
            @Param("examId") String examId);
}