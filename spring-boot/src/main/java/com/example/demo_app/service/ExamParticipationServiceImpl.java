package com.example.demo_app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.ExamParticipation;
import com.example.demo_app.mapper.ExamParticipationMapper;
import com.example.demo_app.service.ExamParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamParticipationServiceImpl extends ServiceImpl<ExamParticipationMapper, ExamParticipation>
        implements ExamParticipationService {

    @Autowired
    private ExamParticipationMapper examParticipationMapper;

    @Override
    public List<ExamParticipation> getExamSubmissions(String examId) {
        return examParticipationMapper.getExamSubmissions(examId);
    }

    @Override
    public List<ExamParticipation> getPendingGrading(String examId) {
        return examParticipationMapper.getPendingGrading(examId);
    }

    @Override
    public List<ExamParticipation> getTeacherExamSubmissions(String teacherId, String examId) {
        return examParticipationMapper.getTeacherExamSubmissions(teacherId, examId);
    }

    @Override
    public boolean submitSubjectiveScore(String studentId, String examId, Float subjectiveScore) {
        // 先查询记录
        ExamParticipation participation = examParticipationMapper.getByCompositeKey(studentId, examId);
        if (participation == null) {
            return false;
        }

        // 更新分数
        Float objectiveScore = participation.getObjectiveScore() != null ? participation.getObjectiveScore() : 0;
        int result = examParticipationMapper.updateSubjectiveScore(
                studentId, examId, subjectiveScore, objectiveScore);

        return result > 0;
    }

    @Override
    public Map<String, Object> getExamStats(String examId) {
        Map<String, Object> stats = new HashMap<>();

        List<ExamParticipation> submissions = getExamSubmissions(examId);

        int totalCount = submissions.size();
        int gradedCount = 0;
        int pendingCount = 0;
        float avgScore = 0;

        for (ExamParticipation sub : submissions) {
            if (sub.getSubjectiveScore() != null) {
                gradedCount++;
                avgScore += (sub.getTotalScore() != null ? sub.getTotalScore() : 0);
            } else {
                pendingCount++;
            }
        }

        if (gradedCount > 0) {
            avgScore = avgScore / gradedCount;
        }

        stats.put("totalCount", totalCount);
        stats.put("gradedCount", gradedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("avgScore", Math.round(avgScore * 100) / 100.0); // 保留两位小数
        stats.put("completionRate", totalCount > 0 ? (gradedCount * 100.0 / totalCount) : 0);

        return stats;
    }

    @Override
    public ExamParticipation getByStudentAndExam(String studentId, String examId) {
        return examParticipationMapper.getByCompositeKey(studentId, examId);
    }
}