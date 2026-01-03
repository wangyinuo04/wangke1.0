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
// 新增（如果没导包的话）
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/homework")
@CrossOrigin // <--- 1. 新增：允许跨域请求（默认允许所有来源）
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
     * 下载作业附件 (已修复跨域和路径参数问题)
     */
    @GetMapping("/download")
    public void downloadHomeworkFile(@RequestParam("filePath") String filePath, HttpServletResponse response) {
        try {
            File file = homeworkService.getHomeworkFile(filePath);

            if (file == null || !file.exists()) {
                response.sendError(404, "文件不存在");
                return;
            }

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);

            // 解决中文文件名乱码
            String fileName = file.getName();
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName);

            response.setContentLengthLong(file.length());

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

    // ================== 学生端接口 ==================

    @Autowired
    private com.example.demo_app.service.StudentService studentService; // 确保引入 StudentService
    @Autowired
    private com.example.demo_app.mapper.TeachingClassMapper teachingClassMapper;
    @Autowired
    private com.example.demo_app.mapper.CourseMapper courseMapper;
    @Autowired
    private com.example.demo_app.mapper.HomeworkSubmissionMapper submissionMapper;
    @Autowired
    private com.example.demo_app.mapper.EnrollmentMapper enrollmentMapper;

    /**
     * 学生获取作业列表（核心逻辑）
     */
    @GetMapping("/student/list")
    public Map<String, Object> getStudentHomeworkList(javax.servlet.http.HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取当前登录学生
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            result.put("success", false);
            result.put("message", "登录已过期");
            return result;
        }
        // 假设 Session 存的是 Student 对象（根据你之前的 LoginController）
        com.example.demo_app.entity.Student student = (com.example.demo_app.entity.Student) userObj;
        String studentId = student.getStudentId();

        try {
            List<com.example.demo_app.entity.StudentHomeworkVO> voList = new java.util.ArrayList<>();

            // 2. 查询该学生加入的所有班级
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.demo_app.entity.Enrollment> enrollQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            enrollQuery.eq("student_id", studentId);
            List<com.example.demo_app.entity.Enrollment> enrollments = enrollmentMapper.selectList(enrollQuery);

            List<String> classIds = new java.util.ArrayList<>();
            for (com.example.demo_app.entity.Enrollment e : enrollments) {
                classIds.add(e.getClassId());
            }

            if (!classIds.isEmpty()) {
                // 3. 查询这些班级布置的所有作业
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Homework> homeworkQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                homeworkQuery.in("class_id", classIds);
                homeworkQuery.orderByDesc("publish_time"); // 按发布时间倒序
                List<Homework> homeworkList = homeworkService.list(homeworkQuery);

                // 4. 组装 VO 数据
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                for (Homework hw : homeworkList) {
                    com.example.demo_app.entity.StudentHomeworkVO vo = new com.example.demo_app.entity.StudentHomeworkVO();

                    // 填充基本信息
                    vo.setId(hw.getHomeworkId());
                    vo.setTitle(hw.getHomeworkTitle());
                    vo.setDeadline(dtf.format(hw.getDeadline()));
                    vo.setTotalScore(hw.getTotalScore());
                    vo.setDescription(hw.getDescription());
                    vo.setAttachment(hw.getAttachmentPath());

                    // 获取课程名称
                    com.example.demo_app.entity.TeachingClass tc = teachingClassMapper.selectById(hw.getClassId());
                    if (tc != null) {
                        com.example.demo_app.entity.Course c = courseMapper.selectById(tc.getCourseId());
                        vo.setCourseName(c != null ? c.getCourseName() : "未知课程");
                    }

                    // 5. 查询该学生的提交记录
                    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission> subQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                    subQuery.eq("homework_id", hw.getHomeworkId())
                            .eq("student_id", studentId);
                    HomeworkSubmission sub = submissionMapper.selectOne(subQuery);

                    // 6. 计算状态
                    LocalDateTime now = LocalDateTime.now();
                    if (sub == null) {
                        // 未提交：判断是否逾期
                        if (now.isAfter(hw.getDeadline())) {
                            vo.setStatus("已逾期");
                        } else {
                            vo.setStatus("未提交");
                        }
                    } else {
                        // 已提交：判断是否已批改
                        vo.setSubmitTime(dtf.format(sub.getSubmissionTime()));
                        vo.setSubmissionContent(sub.getSubmissionText());
                        vo.setSubmissionFile(sub.getAttachmentPath()); // 这里前端可能只需要文件名，后端暂存路径

                        if ("已批改".equals(sub.getGradingStatus())) {
                            vo.setStatus("已批改");
                            vo.setScore(sub.getScore());
                            vo.setFeedback(sub.getTeacherFeedback());
                            // 模拟批改时间，实际表中没有这个字段，可以用更新时间代替
                            vo.setGradeTime(dtf.format(sub.getSubmissionTime().plusDays(1)));
                        } else {
                            vo.setStatus("已提交");
                        }
                    }
                    voList.add(vo);
                }
            }

            result.put("success", true);
            result.put("data", voList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 学生提交作业
     */
    @PostMapping(value = "/student/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> submitHomework(
            @RequestParam("homeworkId") String homeworkId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            javax.servlet.http.HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        String studentId = ((com.example.demo_app.entity.Student) userObj).getStudentId();

        try {
            // 1. 检查是否已经提交过（如果允许覆盖提交，则查询旧记录；如果不允许，则报错）
            // 这里我们实现“覆盖提交”逻辑
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            query.eq("homework_id", homeworkId).eq("student_id", studentId);
            HomeworkSubmission submission = submissionMapper.selectOne(query);

            if (submission == null) {
                submission = new HomeworkSubmission();
                submission.setSubmissionId(java.util.UUID.randomUUID().toString().replace("-", ""));
                submission.setStudentId(studentId);
                submission.setHomeworkId(homeworkId);
                submission.setGradingStatus("未批改");
            } else {
                if ("已批改".equals(submission.getGradingStatus())) {
                    result.put("success", false);
                    result.put("message", "作业已批改，无法重新提交");
                    return result;
                }
            }

            // 2. 处理文件上传
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                String ext = fileName.substring(fileName.lastIndexOf("."));
                // 重命名防止冲突：学号_作业ID_时间戳.后缀
                String newName = studentId + "_" + homeworkId + "_" + System.currentTimeMillis() + ext;

                // 确保目录存在
                String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/homework/submissions/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                file.transferTo(new File(uploadDir + newName));
                submission.setAttachmentPath(newName); // 存文件名或相对路径
            }

            // 3. 保存其他信息
            submission.setSubmissionText(content);
            submission.setSubmissionTime(LocalDateTime.now());
            submission.setSubmissionStatus("已提交");

            // 4. 保存或更新
            if (submissionMapper.selectById(submission.getSubmissionId()) == null) {
                submissionMapper.insert(submission);
            } else {
                submissionMapper.updateById(submission);
            }

            result.put("success", true);
            result.put("message", "提交成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交失败: " + e.getMessage());
        }
        return result;
    }

}