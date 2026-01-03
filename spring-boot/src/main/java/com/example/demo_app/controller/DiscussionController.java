package com.example.demo_app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo_app.entity.*;
import com.example.demo_app.mapper.*;
import com.example.demo_app.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discussion")
// 注意：这里不要加 @CrossOrigin，因为 WebConfig 已经全局配置了
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    // 注入 Mapper 用于直接查询
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private DiscussionTopicMapper topicMapper;
    @Autowired
    private DiscussionReplyMapper replyMapper;
    @Autowired
    private TeachingClassMapper teachingClassMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    // ==========================================
    // Part 1: 学生端接口
    // ==========================================

    /**
     * 学生获取讨论列表
     * 规则：只能看到自己所在班级的话题 + 公共话题
     */
    @GetMapping("/student/list")
    public Map<String, Object> getStudentTopicList(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            result.put("success", false);
            result.put("message", "登录已过期");
            return result;
        }
        Student student = (Student) userObj;

        try {
            // 1. 查询学生加入的所有班级ID
            QueryWrapper<Enrollment> enrollQuery = new QueryWrapper<>();
            enrollQuery.eq("student_id", student.getStudentId());
            List<Enrollment> enrollments = enrollmentMapper.selectList(enrollQuery);
            List<String> classIds = enrollments.stream()
                    .map(Enrollment::getClassId)
                    .collect(Collectors.toList());

            // 2. 构建查询条件：class_id IN (...) OR class_id IS NULL
            QueryWrapper<DiscussionTopic> topicQuery = new QueryWrapper<>();
            topicQuery.and(wrapper -> {
                if (!classIds.isEmpty()) {
                    wrapper.in("class_id", classIds).or();
                }
                wrapper.isNull("class_id").or().eq("class_id", "");
            });

            // 排序：置顶优先，然后按发布时间倒序
            topicQuery.orderByDesc("top_order").orderByDesc("publish_time");

            List<DiscussionTopic> topics = topicMapper.selectList(topicQuery);

            // 3. 填充额外信息
            for (DiscussionTopic topic : topics) {
                fillTopicExtraInfo(topic);
            }

            result.put("success", true);
            result.put("data", topics);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取列表失败: " + e.getMessage());
        }
        return result;
    }

    // ==========================================
    // Part 2: 教师端接口 (解决 404/405 的关键)
    // ==========================================

    /**
     * 教师获取话题列表（支持筛选）
     */
    @GetMapping("/topics")
    public Map<String, Object> getTopics(
            @RequestParam String teacherId,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<DiscussionTopic> query = new QueryWrapper<>();

            // 班级筛选
            if (classId != null && !classId.isEmpty()) {
                query.eq("class_id", classId);
            } else {
                // 如果没指定，查询该老师教的所有班级 + 公共话题
                QueryWrapper<TeachingClass> classQuery = new QueryWrapper<>();
                classQuery.eq("teacher_id", teacherId);
                List<TeachingClass> classes = teachingClassMapper.selectList(classQuery);
                List<String> classIds = classes.stream()
                        .map(TeachingClass::getClassId)
                        .collect(Collectors.toList());

                if (!classIds.isEmpty()) {
                    query.and(wrapper -> wrapper.in("class_id", classIds).or().isNull("class_id"));
                } else {
                    query.isNull("class_id");
                }
            }

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                query.like("topic_title", keyword);
            }

            // 排序
            query.orderByDesc("top_order").orderByDesc("publish_time");

            List<DiscussionTopic> topics = topicMapper.selectList(query);

            // 填充信息
            for (DiscussionTopic topic : topics) {
                fillTopicExtraInfo(topic);
            }

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
     * 话题置顶/取消置顶 (修复 404 的关键！)
     */
    @PutMapping("/topics/{topicId}/top")
    public Map<String, Object> toggleTop(@PathVariable String topicId, @RequestParam Integer topOrder) {
        Map<String, Object> result = new HashMap<>();
        try {
            DiscussionTopic topic = new DiscussionTopic();
            topic.setTopicId(topicId);
            topic.setTopOrder(topOrder);
            topicMapper.updateById(topic);

            result.put("success", true);
            result.put("message", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
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
            topicMapper.deleteById(topicId);
            // 级联删除回复
            replyMapper.delete(new QueryWrapper<DiscussionReply>().eq("topic_id", topicId));

            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 单独获取话题回复列表 (教师端详情页可能用到，修复 404)
     */
    @GetMapping("/topics/{topicId}/replies")
    public Map<String, Object> getReplies(@PathVariable String topicId) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<DiscussionReply> replyQuery = new QueryWrapper<>();
            replyQuery.eq("topic_id", topicId).orderByAsc("publish_time");
            List<DiscussionReply> replies = replyMapper.selectList(replyQuery);

            // 填充作者信息
            for (DiscussionReply reply : replies) {
                fillReplyAuthorInfo(reply);
            }

            result.put("success", true);
            result.put("data", replies);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ==========================================
    // Part 3: 通用接口 (发布、详情、回复)
    // ==========================================

    /**
     * 发布新话题
     */
    @PostMapping("/topics")
    public Map<String, Object> createTopic(@RequestBody DiscussionTopic topic, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Object userObj = session.getAttribute("user");

        try {
            // 自动设置作者
            if (userObj instanceof Student) {
                topic.setAuthorId(((Student) userObj).getStudentId());
            } else if (userObj instanceof Teacher) {
                topic.setAuthorId(((Teacher) userObj).getTeacherId());
            } else {
                // 兼容：如果没有session，尝试检查前端是否传了 authorId (主要用于测试)
                if (topic.getAuthorId() == null) throw new Exception("无法获取登录用户信息");
            }

            topic.setPublishTime(LocalDateTime.now());
            if (topic.getTopOrder() == null) topic.setTopOrder(0);
            if ("".equals(topic.getClassId())) topic.setClassId(null);

            topicMapper.insert(topic);

            result.put("success", true);
            result.put("message", "发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取话题详情 + 回复 (通用)
     */
    @GetMapping("/topics/{topicId}/detail")
    public Map<String, Object> getTopicDetail(@PathVariable String topicId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 获取话题本身
            DiscussionTopic topic = topicMapper.selectById(topicId);
            if (topic == null) throw new Exception("话题不存在");
            fillTopicExtraInfo(topic);

            // 2. 获取回复列表
            QueryWrapper<DiscussionReply> replyQuery = new QueryWrapper<>();
            replyQuery.eq("topic_id", topicId).orderByAsc("publish_time");
            List<DiscussionReply> replies = replyMapper.selectList(replyQuery);

            for (DiscussionReply reply : replies) {
                fillReplyAuthorInfo(reply);
            }

            // 包装返回
            Map<String, Object> data = new HashMap<>();
            data.put("topic", topic);
            data.put("replies", replies);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 发送回复
     */
    @PostMapping("/replies")
    public Map<String, Object> createReply(@RequestBody DiscussionReply reply, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Object userObj = session.getAttribute("user");
        try {
            if (userObj instanceof Student) {
                reply.setAuthorId(((Student) userObj).getStudentId());
            } else if (userObj instanceof Teacher) {
                reply.setAuthorId(((Teacher) userObj).getTeacherId());
            } else {
                if (reply.getAuthorId() == null) throw new Exception("未登录");
            }

            reply.setPublishTime(LocalDateTime.now());
            replyMapper.insert(reply);

            result.put("success", true);
            result.put("message", "回复成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "回复失败: " + e.getMessage());
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
            replyMapper.deleteById(replyId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // --- 辅助方法 ---

    private void fillTopicExtraInfo(DiscussionTopic topic) {
        // 填充课程名
        if (topic.getClassId() != null && !topic.getClassId().isEmpty()) {
            TeachingClass tc = teachingClassMapper.selectById(topic.getClassId());
            if (tc != null) {
                // 尝试获取课程名，如果没有TC关联的Course，就用TC名字
                Course c = courseMapper.selectById(tc.getCourseId());
                topic.setRole(c != null ? c.getCourseName() : tc.getClassName());
            }
        } else {
            topic.setRole("公共讨论区");
        }

        // 填充作者名
        fillAuthorInfo(topic);

        // 填充回复数
        Integer count = replyMapper.selectCount(new QueryWrapper<DiscussionReply>().eq("topic_id", topic.getTopicId()));
        topic.setReplyCount(count);
    }

    private void fillAuthorInfo(DiscussionTopic topic) {
        Teacher t = teacherMapper.selectById(topic.getAuthorId());
        if (t != null) {
            topic.setAuthorName(t.getName() + " (老师)");
        } else {
            Student s = studentMapper.selectById(topic.getAuthorId());
            topic.setAuthorName(s != null ? s.getName() : "未知用户");
        }
    }

    private void fillReplyAuthorInfo(DiscussionReply reply) {
        Teacher t = teacherMapper.selectById(reply.getAuthorId());
        if (t != null) {
            reply.setAuthorName(t.getName() + " (老师)");
        } else {
            Student s = studentMapper.selectById(reply.getAuthorId());
            reply.setAuthorName(s != null ? s.getName() : "未知用户");
        }
    }
}