package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.HomeworkSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmission> {

    @Select("SELECT hs.*, s.name as student_name " +
            "FROM HomeworkSubmission hs " +
            "LEFT JOIN Student s ON hs.student_id = s.student_id " +
            "WHERE hs.homework_id = #{homeworkId} " +
            "ORDER BY hs.submission_time DESC")
    List<HomeworkSubmission> getSubmissionsByHomework(@Param("homeworkId") String homeworkId);

    @Select("SELECT hs.*, s.name as student_name " +
            "FROM HomeworkSubmission hs " +
            "LEFT JOIN Student s ON hs.student_id = s.student_id " +
            "WHERE hs.homework_id = #{homeworkId} AND hs.student_id = #{studentId}")
    HomeworkSubmission getSubmission(@Param("homeworkId") String homeworkId, @Param("studentId") String studentId);

    @Update("UPDATE HomeworkSubmission SET score = #{score}, grading_status = '已批改', teacher_feedback = #{feedback} " +
            "WHERE submission_id = #{submissionId}")
    int gradeHomework(@Param("submissionId") String submissionId, @Param("score") Float score, @Param("feedback") String feedback);
}