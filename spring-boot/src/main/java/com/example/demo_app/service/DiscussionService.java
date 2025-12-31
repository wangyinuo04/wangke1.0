package com.example.demo_app.service;

import com.example.demo_app.entity.DiscussionTopic;
import com.example.demo_app.entity.DiscussionReply;
import java.util.List;
import java.util.Map;

public interface DiscussionService {

    // 话题相关
    List<DiscussionTopic> getTopicsByTeacher(String teacherId, String classId, String keyword);
    DiscussionTopic createTopic(DiscussionTopic topic);
    boolean deleteTopic(String topicId);
    boolean toggleTopTopic(String topicId, Integer topOrder);

    // 回复相关
    List<DiscussionReply> getRepliesByTopic(String topicId);
    DiscussionReply createReply(DiscussionReply reply);
    boolean deleteReply(String replyId);

    // 获取话题详情
    DiscussionTopic getTopicDetail(String topicId);
}