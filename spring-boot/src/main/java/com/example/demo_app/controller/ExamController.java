package com.example.demo_app.controller;

import com.example.demo_app.entity.Exam;
import com.example.demo_app.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 获取考试列表（教师相关）
     */
    @GetMapping("/list")
    public Map<String, Object> getExamList(@RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Exam> exams = examService.getExamsByTeacher(teacherId);
            result.put("success", true);
            result.put("data", exams);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 发布考试
     */
    @PostMapping("/publish")
    public Map<String, Object> publishExam(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 解析请求数据
            Exam exam = new Exam();
            exam.setExamName((String) requestData.get("examName"));
            exam.setClassId((String) requestData.get("classId"));
            exam.setPaperId((String) requestData.get("paperId"));
            exam.setTimeLimit(Integer.parseInt(requestData.get("timeLimit").toString()));
            exam.setShowAnswers(Boolean.parseBoolean(requestData.get("showAnswers").toString()));

            // 解析时间 - 支持多种格式
            String startTimeStr = (String) requestData.get("startTime");
            LocalDateTime startTime;

            try {
                // 尝试解析 ISO 格式 (如 "2026-01-01T02:00")
                if (startTimeStr.contains("T")) {
                    // 使用 LocalDateTime.parse 直接解析 ISO 格式
                    startTime = LocalDateTime.parse(startTimeStr);
                } else {
                    // 尝试解析其他格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    startTime = LocalDateTime.parse(startTimeStr, formatter);
                }
            } catch (Exception e) {
                // 如果以上都失败，尝试最简单的格式
                startTimeStr = startTimeStr.replace("T", " ");
                if (!startTimeStr.contains(":")) {
                    startTimeStr += ":00:00";
                } else if (startTimeStr.split(":").length == 2) {
                    startTimeStr += ":00";
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                startTime = LocalDateTime.parse(startTimeStr, formatter);
            }

            exam.setStartTime(startTime);

            // 不再需要验证教师ID，因为Exam表没有teacher_id字段
            // 通过class_id就可以关联到教学班和教师

            Exam publishedExam = examService.publishExam(exam); // 移除teacherId参数

            result.put("success", true);
            result.put("data", publishedExam);
            result.put("message", "考试发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/delete/{examId}")
    public Map<String, Object> deleteExam(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = examService.removeById(examId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "考试不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取教学班的考试
     */
    @GetMapping("/class/{classId}")
    public Map<String, Object> getExamsByClass(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Exam> exams = examService.getExamsByClass(classId);
            result.put("success", true);
            result.put("data", exams);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}