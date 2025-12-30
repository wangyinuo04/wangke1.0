package com.example.demo_app.controller;

import com.example.demo_app.entity.Chapter;
import com.example.demo_app.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chapters")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    /**
     * 获取教学班的章节树
     */
    @GetMapping("/tree/{classId}")
    public Map<String, Object> getChapterTree(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> chapterTree = chapterService.getChapterTree(classId);
            result.put("success", true);
            result.put("data", chapterTree);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取教学班的所有章节（平铺列表）
     */
    @GetMapping("/list/{classId}")
    public Map<String, Object> getChaptersByClass(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Chapter> chapters = chapterService.getChaptersByClassId(classId);
            result.put("success", true);
            result.put("data", chapters);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 新增章节
     */
    @PostMapping("/add")
    public Map<String, Object> addChapter(@RequestBody Chapter chapter) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = chapterService.addChapter(chapter);
            if (success) {
                result.put("success", true);
                result.put("data", chapter.getChapterId());
                result.put("message", "添加章节成功");
            } else {
                result.put("success", false);
                result.put("message", "添加章节失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新章节
     */
    @PutMapping("/update")
    public Map<String, Object> updateChapter(@RequestBody Chapter chapter) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = chapterService.updateChapter(chapter);
            if (success) {
                result.put("success", true);
                result.put("message", "更新章节成功");
            } else {
                result.put("success", false);
                result.put("message", "章节不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/delete/{chapterId}")
    public Map<String, Object> deleteChapter(@PathVariable String chapterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = chapterService.deleteChapter(chapterId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除章节成功");
            } else {
                result.put("success", false);
                result.put("message", "章节不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取章节详情
     */
    @GetMapping("/detail/{chapterId}")
    public Map<String, Object> getChapterDetail(@PathVariable String chapterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Chapter chapter = chapterService.getById(chapterId);
            if (chapter != null) {
                result.put("success", true);
                result.put("data", chapter);
                result.put("message", "查询成功");
            } else {
                result.put("success", false);
                result.put("message", "章节不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }
}