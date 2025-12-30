package com.example.demo_app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Homework;
import com.example.demo_app.entity.HomeworkSubmission;
import com.example.demo_app.mapper.HomeworkMapper;
import com.example.demo_app.mapper.HomeworkSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class HomeworkService extends ServiceImpl<HomeworkMapper, Homework> {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    /**
     * 获取教师的所有作业
     */
    public List<Homework> getHomeworkListByTeacher(String teacherId) {
        List<Homework> homeworks = homeworkMapper.getHomeworkListByTeacher(teacherId);

        // 计算状态
        LocalDateTime now = LocalDateTime.now();
        for (Homework hw : homeworks) {
            if (hw.getPublishTime() != null && hw.getDeadline() != null) {
                if (now.isBefore(hw.getPublishTime())) {
                    hw.setStatus("未发布");
                } else if (now.isAfter(hw.getDeadline())) {
                    hw.setStatus("已结束");
                } else {
                    hw.setStatus("进行中");
                }
            }
        }

        return homeworks;
    }

    /**
     * 发布作业
     */
    @Transactional
    public Homework publishHomework(Homework homework, MultipartFile attachmentFile) throws IOException {
        // 生成作业ID
        homework.setHomeworkId(UUID.randomUUID().toString());
        // 移除了 homework.setCreateTime(LocalDateTime.now());

        // 处理附件
        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            String filePath = saveHomeworkFile(attachmentFile, "assignments");
            homework.setAttachmentPath(filePath);
        }

        // 保存到数据库
        save(homework);
        return homework;
    }

    /**
     * 获取作业详情
     */
    public Homework getHomeworkDetail(String homeworkId) {
        Homework homework = getById(homeworkId);
        if (homework != null) {
            // 获取提交统计
            List<HomeworkSubmission> submissions = submissionMapper.getSubmissionsByHomework(homeworkId);
            homework.setSubmittedCount((int) submissions.stream()
                    .filter(s -> "已提交".equals(s.getSubmissionStatus()))
                    .count());
        }
        return homework;
    }

    /**
     * 获取作业提交列表
     */
    public List<HomeworkSubmission> getHomeworkSubmissions(String homeworkId) {
        return submissionMapper.getSubmissionsByHomework(homeworkId);
    }

    /**
     * 批改作业
     */
    @Transactional
    public boolean gradeHomework(String submissionId, Float score, String feedback) {
        HomeworkSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return false;
        }

        submission.setScore(score);
        submission.setGradingStatus("已批改");
        submission.setTeacherFeedback(feedback);

        return submissionMapper.updateById(submission) > 0;
    }

    /**
     * 批量下载作业
     */
    public File getHomeworkFilesZip(String homeworkId) {
        // 这里需要实现将作业附件打包成ZIP的逻辑
        // 由于时间关系，先返回null，你可以根据需要实现
        return null;
    }

    /**
     * 保存作业文件
     */
    private String saveHomeworkFile(MultipartFile file, String subDir) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // 构建存储路径
        String projectRoot = System.getProperty("user.dir");
        String storagePath = Paths.get(projectRoot, "src/main/resources", uploadDir, "homework", subDir).toString();

        // 创建目录
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        Path filePath = Paths.get(storagePath, newFilename);
        file.transferTo(filePath.toFile());

        // 返回相对路径
        return "/uploads/homework/" + subDir + "/" + newFilename;
    }

    /**
     * 获取作业附件文件
     */
    public File getHomeworkFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // 将相对路径转换为绝对路径
        if (filePath.startsWith("/uploads/")) {
            String projectRoot = System.getProperty("user.dir");
            filePath = Paths.get(projectRoot, "src/main/resources", filePath.substring(1)).toString();
        }

        File file = new File(filePath);
        return file.exists() ? file : null;
    }
}