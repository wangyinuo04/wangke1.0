package com.example.demo_app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Exam;
import com.example.demo_app.mapper.ExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExamService extends ServiceImpl<ExamMapper, Exam> {

    @Autowired
    private ExamMapper examMapper;

    /**
     * 获取教师相关的考试列表
     */
    public List<Exam> getExamsByTeacher(String teacherId) {
        return examMapper.getExamsByTeacher(teacherId);
    }

    /**
     * 发布考试
     */
    @Transactional
    public Exam publishExam(Exam exam, String teacherId) {
        // 生成考试ID
        exam.setExamId(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        exam.setTeacherId(teacherId);
        exam.setCreatedTime(LocalDateTime.now());

        // 计算结束时间
        LocalDateTime startTime = exam.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(exam.getTimeLimit());
        exam.setEndTime(endTime);

        // 保存考试
        save(exam);

        return exam;
    }

    /**
     * 获取教学班的考试
     */
    public List<Exam> getExamsByClass(String classId) {
        return examMapper.getExamsByClass(classId);
    }
}