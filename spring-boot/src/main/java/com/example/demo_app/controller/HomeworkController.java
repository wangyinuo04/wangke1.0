package com.example.demo_app.controller;

import com.example.demo_app.entity.Homework;
import com.example.demo_app.entity.HomeworkSubmission;
import com.example.demo_app.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homework")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    /**
     * 获取教师发布的作业列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Map<String, Object> getHomeworkList(@PathVariable String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Homework> homeworks = homeworkService.getHomeworkListByTeacher(teacherId);
            result.put("success", true);
            result.put("data", homeworks);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 发布作业（支持附件上传）
     */
    @PostMapping(value = "/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> publishHomework(
            @RequestParam("homeworkTitle") String homeworkTitle,
            @RequestParam("description") String description,
            @RequestParam("classId") String classId,
            @RequestParam("totalScore") Float totalScore,
            @RequestParam("publishTime") String publishTimeStr,
            @RequestParam("deadline") String deadlineStr,
            @RequestParam(value = "attachmentFile", required = false) MultipartFile attachmentFile) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 创建作业对象
            Homework homework = new Homework();
            homework.setHomeworkTitle(homeworkTitle);
            homework.setDescription(description);
            homework.setClassId(classId);
            homework.setTotalScore(totalScore);
            homework.setPublishTime(LocalDateTime.parse(publishTimeStr.replace(" ", "T")));
            homework.setDeadline(LocalDateTime.parse(deadlineStr.replace(" ", "T")));

            // 发布作业
            Homework savedHomework = homeworkService.publishHomework(homework, attachmentFile);

            result.put("success", true);
            result.put("data", savedHomework);
            result.put("message", "作业发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/detail/{homeworkId}")
    public Map<String, Object> getHomeworkDetail(@PathVariable String homeworkId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Homework homework = homeworkService.getHomeworkDetail(homeworkId);
            if (homework != null) {
                result.put("success", true);
                result.put("data", homework);
                result.put("message", "查询成功");
            } else {
                result.put("success", false);
                result.put("message", "作业不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取作业提交列表
     */
    @GetMapping("/submissions/{homeworkId}")
    public Map<String, Object> getSubmissions(@PathVariable String homeworkId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HomeworkSubmission> submissions = homeworkService.getHomeworkSubmissions(homeworkId);
            result.put("success", true);
            result.put("data", submissions);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批改作业
     */
    @PostMapping("/grade")
    public Map<String, Object> gradeHomework(
            @RequestParam("submissionId") String submissionId,
            @RequestParam("score") Float score,
            @RequestParam(value = "feedback", required = false) String feedback) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = homeworkService.gradeHomework(submissionId, score, feedback);
            if (success) {
                result.put("success", true);
                result.put("message", "批改成功");
            } else {
                result.put("success", false);
                result.put("message", "批改失败，提交记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "批改失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 下载作业附件
     */
    @GetMapping("/download/{filePath}")
    public void downloadHomeworkFile(@PathVariable String filePath, HttpServletResponse response) {
        try {
            File file = homeworkService.getHomeworkFile(filePath);
            if (file == null || !file.exists()) {
                response.sendError(404, "文件不存在");
                return;
            }

            // 设置响应头
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLengthLong(file.length());

            // 传输文件
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, "下载失败: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 批量下载作业附件
     */
    @GetMapping("/batch-download/{homeworkId}")
    public void batchDownloadHomework(@PathVariable String homeworkId, HttpServletResponse response) {
        try {
            File zipFile = homeworkService.getHomeworkFilesZip(homeworkId);
            if (zipFile == null) {
                response.sendError(404, "暂无提交的作业文件");
                return;
            }

            response.setContentType("application/zip");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"homework_" + homeworkId + ".zip\"");
            response.setContentLengthLong(zipFile.length());

            try (InputStream inputStream = new FileInputStream(zipFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, "打包下载失败: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/delete/{homeworkId}")
    public Map<String, Object> deleteHomework(@PathVariable String homeworkId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = homeworkService.removeById(homeworkId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "作业不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}