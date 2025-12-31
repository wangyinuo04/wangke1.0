package com.example.demo_app.controller;

import com.example.demo_app.entity.DiscussionTopic;
import com.example.demo_app.entity.DiscussionReply;
import com.example.demo_app.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussion")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    /**
     * 获取教师的所有讨论话题（支持按班级筛选和搜索）
     */
    @GetMapping("/topics")
    public Map<String, Object> getTopics(@RequestParam String teacherId,
                                         @RequestParam(required = false) String classId,
                                         @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DiscussionTopic> topics = discussionService.getTopicsByTeacher(teacherId, classId, keyword);
            result.put("success", true);
            result.put("data", topics);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建新话题
     */
    @PostMapping("/topics")
    public Map<String, Object> createTopic(@RequestBody DiscussionTopic topic) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiscussionTopic newTopic = discussionService.createTopic(topic);
            result.put("success", true);
            result.put("data", newTopic);
            result.put("message", "发布话题成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除话题
     */
    @DeleteMapping("/topics/{topicId}")
    public Map<String, Object> deleteTopic(@PathVariable String topicId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = discussionService.deleteTopic(topicId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 置顶/取消置顶话题
     */
    @PutMapping("/topics/{topicId}/top")
    public Map<String, Object> toggleTop(@PathVariable String topicId,
                                         @RequestParam Integer topOrder) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = discussionService.toggleTopTopic(topicId, topOrder);
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取话题详情
     */
    @GetMapping("/topics/{topicId}/detail")
    public Map<String, Object> getTopicDetail(@PathVariable String topicId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiscussionTopic topic = discussionService.getTopicDetail(topicId);
            result.put("success", true);
            result.put("data", topic);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取话题的所有回复
     */
    @GetMapping("/topics/{topicId}/replies")
    public Map<String, Object> getReplies(@PathVariable String topicId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DiscussionReply> replies = discussionService.getRepliesByTopic(topicId);
            result.put("success", true);
            result.put("data", replies);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 创建回复
     */
    @PostMapping("/replies")
    public Map<String, Object> createReply(@RequestBody DiscussionReply reply) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiscussionReply newReply = discussionService.createReply(reply);
            result.put("success", true);
            result.put("data", newReply);
            result.put("message", "回复成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "回复失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public Map<String, Object> deleteReply(@PathVariable String replyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = discussionService.deleteReply(replyId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}