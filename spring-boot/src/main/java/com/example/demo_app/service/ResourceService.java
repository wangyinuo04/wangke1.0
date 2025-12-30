package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Resource;
import com.example.demo_app.mapper.ResourceMapper;
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
public class ResourceService extends ServiceImpl<ResourceMapper, Resource> {

    @Value("${file.upload-dir:uploads/resources}")
    private String uploadDir;

    @Autowired
    private ChapterService chapterService;

    /**
     * 上传资源文件
     */
    @Transactional
    public Resource uploadResource(MultipartFile file, Resource resource) throws IOException {
        // 验证章节是否存在
        if (resource.getChapterId() == null) {
            throw new RuntimeException("章节ID不能为空");
        }

        // 生成资源ID
        resource.setResourceId(UUID.randomUUID().toString());

        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成新的文件名（防止重复）
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 根据资源类型确定存储子目录
            String typeSubDir = getTypeSubDirectory(resource.getResourceType());

            // 获取项目的根目录路径
            String projectRoot = System.getProperty("user.dir");
            String storagePath = Paths.get(projectRoot, "src/main/resources", uploadDir, typeSubDir).toString();

            // 创建目录
            File dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(storagePath, newFilename);
            file.transferTo(filePath.toFile());

            // 设置文件信息
            resource.setResourceName(originalFilename);
            // 设置相对路径，用于前端访问
            resource.setFilePath("/uploads/resources/" + typeSubDir + "/" + newFilename);
            resource.setFileSize(file.getSize());
            // 注意：formattedSize是临时字段，不需要保存到数据库
        }

        // 设置上传时间
        resource.setUploadTime(LocalDateTime.now());
        // 注意：formattedUploadTime是临时字段，不需要保存到数据库

        // 保存到数据库（只会保存实体类中标注了@TableField的字段）
        save(resource);

        // 设置临时字段用于返回
        resource.setFormattedSize(formatFileSize(resource.getFileSize()));
        resource.setFormattedUploadTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(resource.getUploadTime()));

        return resource;
    }

    /**
     * 更新资源信息（不更新文件）
     */
    @Transactional
    public boolean updateResourceInfo(Resource resource) {
        Resource existing = getById(resource.getResourceId());
        if (existing == null) {
            return false;
        }

        // 只更新允许修改的字段
        existing.setResourceName(resource.getResourceName());
        existing.setAllowDownload(resource.getAllowDownload());

        return updateById(existing);
    }

    /**
     * 删除资源（包括物理文件）
     */
    @Transactional
    public boolean deleteResource(String resourceId) {
        try {
            Resource resource = getById(resourceId);
            if (resource == null) {
                return false;
            }

            // 删除物理文件
            if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
                String filePath = resource.getFilePath();
                // 将相对路径转换为绝对路径
                if (filePath.startsWith("/uploads/")) {
                    String projectRoot = System.getProperty("user.dir");
                    filePath = Paths.get(projectRoot, "src/main/resources", filePath.substring(1)).toString();
                }
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }

            // 删除数据库记录
            return removeById(resourceId);
        } catch (Exception e) {
            throw new RuntimeException("删除资源失败: " + e.getMessage());
        }
    }

    /**
     * 根据章节ID获取资源列表
     */
    public List<Resource> getResourcesByChapterId(String chapterId) {
        List<Resource> resources = this.baseMapper.selectResourcesWithChapterName(chapterId);

        // 格式化时间和文件大小（设置临时字段）
        for (Resource resource : resources) {
            if (resource.getUploadTime() != null) {
                resource.setFormattedUploadTime(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(resource.getUploadTime())
                );
            }
            if (resource.getFileSize() != null) {
                resource.setFormattedSize(formatFileSize(resource.getFileSize()));
            }
        }

        return resources;
    }

    /**
     * 根据资源类型获取存储子目录
     */
    private String getTypeSubDirectory(String resourceType) {
        switch (resourceType) {
            case "视频":
                return "videos";
            case "PDF":
                return "pdfs";
            case "PPT":
                return "ppts";
            case "Word":
                return "docs";
            case "音频":
                return "audios";
            default:
                return "others";
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取文件对象
     */
    public File getResourceFile(String resourceId) {
        Resource resource = getById(resourceId);
        if (resource == null || resource.getFilePath() == null) {
            return null;
        }

        String filePath = resource.getFilePath();
        // 将相对路径转换为绝对路径
        if (filePath.startsWith("/uploads/")) {
            String projectRoot = System.getProperty("user.dir");
            filePath = Paths.get(projectRoot, "src/main/resources", filePath.substring(1)).toString();
        }

        File file = new File(filePath);
        return file.exists() ? file : null;
    }
}