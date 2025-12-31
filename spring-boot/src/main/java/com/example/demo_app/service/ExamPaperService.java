package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.ExamPaper;
import com.example.demo_app.entity.PaperQuestion;
import com.example.demo_app.entity.Question;
import com.example.demo_app.mapper.ExamPaperMapper;
import com.example.demo_app.mapper.PaperQuestionMapper;
import com.example.demo_app.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExamPaperService extends ServiceImpl<ExamPaperMapper, ExamPaper> {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperQuestionService paperQuestionService;

    /**
     * 获取教师相关的试卷列表
     */
    public List<ExamPaper> getPapersByTeacher(String teacherId) {
        return examPaperMapper.getPapersByTeacher(teacherId);
    }

    /**
     * 创建试卷
     */
    @Transactional
    public ExamPaper createPaper(ExamPaper paper, List<String> questionIds) {
        // 生成试卷ID
        paper.setPaperId(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        paper.setPaperStatus("草稿");
        paper.setCreatedTime(LocalDateTime.now());

        // 计算总分
        float totalScore = calculateTotalScore(questionIds);
        paper.setTotalScore(totalScore);

        // 保存试卷
        save(paper);

        // 保存试卷-试题关联
        savePaperQuestions(paper.getPaperId(), questionIds);

        return paper;
    }

    /**
     * 计算试卷总分
     */
    private float calculateTotalScore(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0f;
        }

        try {
            // 使用QuestionMapper的selectBatchIds方法查询试题分数
            List<Question> questions = questionMapper.selectBatchIds(questionIds);
            if (questions != null && !questions.isEmpty()) {
                return (float) questions.stream()
                        .mapToDouble(q -> {
                            Float score = q.getScore();
                            return score != null ? score : 0.0;
                        })
                        .sum();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    /**
     * 保存试卷-试题关联
     */
    private void savePaperQuestions(String paperId, List<String> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(questionIds.get(i));
            pq.setQuestionOrder(i + 1);
            paperQuestionMapper.insert(pq);
        }
    }

    /**
     * 删除试卷（先删除关联的试题）
     */
    @Transactional
    public boolean deletePaperWithQuestions(String paperId) {
        try {
            // 1. 先删除PaperQuestion表中的关联记录
            LambdaQueryWrapper<PaperQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PaperQuestion::getPaperId, paperId);
            paperQuestionService.remove(wrapper);

            // 2. 再删除试卷
            return removeById(paperId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取已发布的试卷
     */
    public List<ExamPaper> getPublishedPapersByCourse(String courseId) {
        return examPaperMapper.getPublishedPapersByCourse(courseId);
    }

    /**
     * 发布试卷
     */
    @Transactional
    public boolean publishPaper(String paperId) {
        ExamPaper paper = getById(paperId);
        if (paper == null) {
            return false;
        }
        paper.setPaperStatus("已发布");
        return updateById(paper);
    }

    /**
     * 获取教师相关的试卷列表（按课程分组）
     */
    public Map<String, List<ExamPaper>> getPapersGroupedByCourse(String teacherId) {
        // 获取所有试卷
        List<ExamPaper> allPapers = examPaperMapper.getPapersByTeacher(teacherId);

        // 按课程ID分组
        Map<String, List<ExamPaper>> groupedPapers = new LinkedHashMap<>();

        for (ExamPaper paper : allPapers) {
            String courseId = paper.getCourseId();
            groupedPapers.computeIfAbsent(courseId, k -> new ArrayList<>()).add(paper);
        }

        return groupedPapers;
    }
}