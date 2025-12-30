package com.example.demo_app.controller;

import com.example.demo_app.entity.Resource;
import com.example.demo_app.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 上传资源（支持文件上传）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("resourceName") String resourceName,
            @RequestParam("resourceType") String resourceType,
            @RequestParam("chapterId") String chapterId,
            @RequestParam(value = "allowDownload", defaultValue = "true") boolean allowDownload) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 创建资源对象
            Resource resource = new Resource();
            resource.setResourceName(resourceName);
            resource.setResourceType(resourceType);
            resource.setChapterId(chapterId);
            resource.setAllowDownload(allowDownload);

            // 调用服务层上传
            Resource savedResource = resourceService.uploadResource(file, resource);

            result.put("success", true);
            result.put("data", savedResource);
            result.put("message", "资源上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据章节ID获取资源列表
     */
    @GetMapping("/chapter/{chapterId}")
    public Map<String, Object> getResourcesByChapter(@PathVariable String chapterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Resource> resources = resourceService.getResourcesByChapterId(chapterId);
            result.put("success", true);
            result.put("data", resources);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新资源信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateResource(@RequestBody Resource resource) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = resourceService.updateResourceInfo(resource);
            if (success) {
                result.put("success", true);
                result.put("message", "更新资源成功");
            } else {
                result.put("success", false);
                result.put("message", "资源不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/delete/{resourceId}")
    public Map<String, Object> deleteResource(@PathVariable String resourceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = resourceService.deleteResource(resourceId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除资源成功");
            } else {
                result.put("success", false);
                result.put("message", "资源不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 下载资源文件 - 简化版，避免类型冲突
     */
    @GetMapping("/download/{resourceId}")
    public void downloadResource(@PathVariable String resourceId, HttpServletResponse response) {
        try {
            File file = resourceService.getResourceFile(resourceId);
            if (file == null || !file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }

            // 设置响应头
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");
            response.setContentLengthLong(file.length());

            // 使用流式传输文件
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
                response.getOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载失败: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/detail/{resourceId}")
    public Map<String, Object> getResourceDetail(@PathVariable String resourceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Resource resource = resourceService.getById(resourceId);
            if (resource != null) {
                // 格式化文件大小
                if (resource.getFileSize() != null) {
                    long size = resource.getFileSize();
                    if (size < 1024) {
                        resource.setFormattedSize(size + " B");
                    } else if (size < 1024 * 1024) {
                        resource.setFormattedSize(String.format("%.1f KB", size / 1024.0));
                    } else if (size < 1024 * 1024 * 1024) {
                        resource.setFormattedSize(String.format("%.1f MB", size / (1024.0 * 1024.0)));
                    } else {
                        resource.setFormattedSize(String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0)));
                    }
                }

                result.put("success", true);
                result.put("data", resource);
                result.put("message", "查询成功");
            } else {
                result.put("success", false);
                result.put("message", "资源不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }
}