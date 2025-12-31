package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.DiscussionReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DiscussionReplyMapper extends BaseMapper<DiscussionReply> {

    // 根据话题ID获取回复列表（关联查询作者姓名和引用作者姓名）
    @Select("SELECT dr.*, " +
            "CASE WHEN t.name IS NOT NULL THEN t.name ELSE s.name END as author_name, " +
            "CASE WHEN t.name IS NOT NULL THEN 'teacher' ELSE 'student' END as role, " +
            "CASE WHEN dr.quote_author_id IS NOT NULL THEN " +
            "  CASE WHEN t2.name IS NOT NULL THEN t2.name ELSE s2.name END " +
            "END as quote_author_name " +
            "FROM DiscussionReply dr " +
            "LEFT JOIN Teacher t ON dr.author_id = t.teacher_id " +
            "LEFT JOIN Student s ON dr.author_id = s.student_id " +
            "LEFT JOIN Teacher t2 ON dr.quote_author_id = t2.teacher_id " +
            "LEFT JOIN Student s2 ON dr.quote_author_id = s2.student_id " +
            "WHERE dr.topic_id = #{topicId} " +
            "ORDER BY dr.publish_time ASC")
    List<DiscussionReply> selectByTopicId(@Param("topicId") String topicId);
}