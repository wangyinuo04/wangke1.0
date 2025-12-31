package com.example.demo_app.controller;

import com.example.demo_app.entity.ExamPaper;
import com.example.demo_app.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paper")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 获取试卷列表（教师相关）
     */
    @GetMapping("/list")
    public Map<String, Object> getPaperList(@RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ExamPaper> papers = examPaperService.getPapersByTeacher(teacherId);
            result.put("success", true);
            result.put("data", papers);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取按课程分组的试卷列表
     */
    @GetMapping("/grouped-by-course")
    public Map<String, Object> getPapersGroupedByCourse(@RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, List<ExamPaper>> groupedPapers = examPaperService.getPapersGroupedByCourse(teacherId);
            result.put("success", true);
            result.put("data", groupedPapers);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建试卷
     */
    @PostMapping("/create")
    public Map<String, Object> createPaper(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 解析请求数据
            ExamPaper paper = new ExamPaper();
            paper.setPaperTitle((String) requestData.get("paperTitle"));
            paper.setCourseId((String) requestData.get("courseId"));

            List<String> questionIds = (List<String>) requestData.get("questionIds");

            if (questionIds == null || questionIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "请至少选择一道题目");
                return result;
            }

            ExamPaper createdPaper = examPaperService.createPaper(paper, questionIds);

            result.put("success", true);
            result.put("data", createdPaper);
            result.put("message", "试卷创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 发布试卷
     */
    @PutMapping("/publish/{paperId}")
    public Map<String, Object> publishPaper(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = examPaperService.publishPaper(paperId);
            if (success) {
                result.put("success", true);
                result.put("message", "发布成功");
            } else {
                result.put("success", false);
                result.put("message", "试卷不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/delete/{paperId}")
    public Map<String, Object> deletePaper(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = examPaperService.deletePaperWithQuestions(paperId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "试卷不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取课程相关的已发布试卷
     */
    @GetMapping("/published/{courseId}")
    public Map<String, Object> getPublishedPapers(@PathVariable String courseId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ExamPaper> papers = examPaperService.getPublishedPapersByCourse(courseId);
            result.put("success", true);
            result.put("data", papers);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}