package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Question;
import com.example.demo_app.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// QuestionService.java
@Service
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 获取教师相关的试题列表
     */
    public List<Question> getQuestionsByTeacher(String teacherId, String keyword) {
        // 如果传入了教师ID，调用Mapper方法
        if (teacherId != null && !teacherId.trim().isEmpty()) {
            return questionMapper.getQuestionsByTeacher(teacherId, keyword);
        }
        // 否则获取所有试题
        return list();
    }

    /**
     * 获取教学班相关的试题
     */
    public List<Question> getQuestionsByClass(String classId) {
        // 这里需要先获取教学班的课程ID
        // 由于没有TeachingClassService，暂时返回空列表
        // 或者可以创建一个简单的查询
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        // 这里需要根据classId找到对应的courseId，暂时返回空
        return new ArrayList<>();
    }

    /**
     * 新增试题
     */
    @Transactional
    public boolean addQuestion(Question question, String teacherId) {
        // 生成试题ID
        question.setQuestionId(UUID.randomUUID().toString().replace("-", "").substring(0, 20));

        return save(question);
    }

    /**
     * 更新试题
     */
    @Transactional
    public boolean updateQuestion(Question question) {
        Question existing = getById(question.getQuestionId());
        if (existing == null) {
            return false;
        }

        // 更新允许修改的字段
        existing.setContent(question.getContent());
        existing.setQuestionType(question.getQuestionType());
        existing.setOptions(question.getOptions());
        existing.setCorrectAnswer(question.getCorrectAnswer());
        existing.setExplanation(question.getExplanation());
        existing.setDifficulty(question.getDifficulty());
        existing.setScore(question.getScore());
        existing.setCourseId(question.getCourseId());

        return updateById(existing);
    }

    /**
     * 获取课程相关的试题
     */
    public List<Question> getQuestionsByCourse(String courseId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getCourseId, courseId);
        wrapper.orderByDesc(Question::getQuestionId);
        return list(wrapper);
    }
}