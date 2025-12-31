package com.example.demo_app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo_app.entity.ExamParticipation;
import java.util.List;
import java.util.Map;

public interface ExamParticipationService extends IService<ExamParticipation> {

    /**
     * 获取考试的所有参与记录
     */
    List<ExamParticipation> getExamSubmissions(String examId);

    /**
     * 获取需要批改的主观题
     */
    List<ExamParticipation> getPendingGrading(String examId);

    /**
     * 获取教师管理的考试参与记录
     */
    List<ExamParticipation> getTeacherExamSubmissions(String teacherId, String examId);

    /**
     * 提交主观题评分（复合主键版本）
     */
    boolean submitSubjectiveScore(String studentId, String examId, Float subjectiveScore);

    /**
     * 获取考试统计数据
     */
    Map<String, Object> getExamStats(String examId);

    /**
     * 根据学生ID和考试ID获取参与记录
     */
    ExamParticipation getByStudentAndExam(String studentId, String examId);
}