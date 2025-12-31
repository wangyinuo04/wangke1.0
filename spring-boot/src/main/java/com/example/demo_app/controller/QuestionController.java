package com.example.demo_app.controller;

import com.example.demo_app.entity.Question;
import com.example.demo_app.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/list")
    public Map<String, Object> getQuestionList(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Question> questions;
            if (classId != null && !classId.trim().isEmpty()) {
                // 按教学班获取试题 - 这里需要实现具体的逻辑
                // 由于目前没有TeachingClass表关联，暂时返回空
                questions = new ArrayList<>();
            } else {
                // 获取教师的所有试题
                questions = questionService.getQuestionsByTeacher(teacherId, null);
            }
            result.put("success", true);
            result.put("data", questions);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 新增试题
     */
    @PostMapping("/add")
    public Map<String, Object> addQuestion(@RequestBody Question question, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求中获取教师ID（实际应该从token或session获取）
            String teacherId = request.getHeader("X-Teacher-Id");
            if (teacherId == null) {
                // 从请求参数获取
                teacherId = request.getParameter("teacherId");
            }

            if (teacherId == null || teacherId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "教师ID不能为空");
                return result;
            }

            boolean success = questionService.addQuestion(question, teacherId);
            if (success) {
                result.put("success", true);
                result.put("message", "新增试题成功");
                result.put("data", question.getQuestionId());
            } else {
                result.put("success", false);
                result.put("message", "新增试题失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "新增失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新试题
     */
    @PutMapping("/update")
    public Map<String, Object> updateQuestion(@RequestBody Question question) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = questionService.updateQuestion(question);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "试题不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除试题
     */
    @DeleteMapping("/delete/{questionId}")
    public Map<String, Object> deleteQuestion(@PathVariable String questionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = questionService.removeById(questionId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "试题不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取课程相关的试题
     */
    @GetMapping("/course/{courseId}")
    public Map<String, Object> getQuestionsByCourse(@PathVariable String courseId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Question> questions = questionService.getQuestionsByCourse(courseId);
            result.put("success", true);
            result.put("data", questions);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}