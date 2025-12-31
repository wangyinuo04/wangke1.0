package com.example.demo_app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.DiscussionTopic;
import com.example.demo_app.entity.DiscussionReply;
import com.example.demo_app.mapper.DiscussionTopicMapper;
import com.example.demo_app.mapper.DiscussionReplyMapper;
import com.example.demo_app.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiscussionServiceImpl extends ServiceImpl<DiscussionTopicMapper, DiscussionTopic> implements DiscussionService {

    @Autowired
    private DiscussionTopicMapper topicMapper;

    @Autowired
    private DiscussionReplyMapper replyMapper;

    @Override
    public List<DiscussionTopic> getTopicsByTeacher(String teacherId, String classId, String keyword) {
        if (classId != null && !classId.isEmpty()) {
            return topicMapper.selectByClassId(classId);
        } else {
            return topicMapper.searchTopics(teacherId, classId, keyword);
        }
    }

    @Override
    @Transactional
    public DiscussionTopic createTopic(DiscussionTopic topic) {
        topic.setPublishTime(LocalDateTime.now());

        // 保存话题（只插入数据库实际存在的字段）
        topicMapper.insert(topic);

        // 重新查询获取完整信息（包含作者姓名等）
        return topicMapper.selectTopicDetail(topic.getTopicId());
    }

    @Override
    @Transactional
    public boolean deleteTopic(String topicId) {
        // 先删除所有回复
        QueryWrapper<DiscussionReply> wrapper = new QueryWrapper<>();
        wrapper.eq("topic_id", topicId);
        replyMapper.delete(wrapper);

        // 删除话题
        int result = topicMapper.deleteById(topicId);
        return result > 0;
    }

    @Override
    public boolean toggleTopTopic(String topicId, Integer topOrder) {
        DiscussionTopic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            return false;
        }

        topic.setTopOrder(topOrder);
        int result = topicMapper.updateById(topic);
        return result > 0;
    }

    @Override
    public List<DiscussionReply> getRepliesByTopic(String topicId) {
        return replyMapper.selectByTopicId(topicId);
    }

    @Override
    @Transactional
    public DiscussionReply createReply(DiscussionReply reply) {
        reply.setPublishTime(LocalDateTime.now());

        // 保存回复（包含引用字段）
        replyMapper.insert(reply);

        // 重新查询获取完整信息（包含作者姓名、引用作者姓名等）
        return replyMapper.selectById(reply.getReplyId());
    }

    @Override
    @Transactional
    public boolean deleteReply(String replyId) {
        DiscussionReply reply = replyMapper.selectById(replyId);
        if (reply == null) {
            return false;
        }

        int result = replyMapper.deleteById(replyId);
        return result > 0;
    }

    @Override
    public DiscussionTopic getTopicDetail(String topicId) {
        // 使用专门的查询方法获取话题详情（包含作者姓名、角色等信息）
        DiscussionTopic topic = topicMapper.selectTopicDetail(topicId);

        // 注意：view_count不是数据库字段，不需要更新
        // 如果需要在每次查看时记录，可以在前端处理或使用其他方式

        return topic;
    }
}