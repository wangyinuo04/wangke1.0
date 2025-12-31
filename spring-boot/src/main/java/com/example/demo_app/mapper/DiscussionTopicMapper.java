package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.DiscussionTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DiscussionTopicMapper extends BaseMapper<DiscussionTopic> {

    // 根据班级ID获取话题列表（关联查询作者姓名）
    @Select("SELECT dt.*, " +
            "CASE WHEN t.name IS NOT NULL THEN t.name ELSE s.name END as author_name, " +
            "CASE WHEN t.name IS NOT NULL THEN 'teacher' ELSE 'student' END as role, " +
            "(SELECT COUNT(*) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as reply_count, " +
            "(SELECT MAX(publish_time) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as last_reply_time " +
            "FROM DiscussionTopic dt " +
            "LEFT JOIN Teacher t ON dt.author_id = t.teacher_id " +
            "LEFT JOIN Student s ON dt.author_id = s.student_id " +
            "WHERE dt.class_id = #{classId} " +
            "ORDER BY dt.top_order DESC, dt.publish_time DESC")
    List<DiscussionTopic> selectByClassId(@Param("classId") String classId);

    // 根据教师ID获取所有班级的话题
    @Select("SELECT dt.*, " +
            "CASE WHEN t.name IS NOT NULL THEN t.name ELSE s.name END as author_name, " +
            "CASE WHEN t.name IS NOT NULL THEN 'teacher' ELSE 'student' END as role, " +
            "(SELECT COUNT(*) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as reply_count, " +
            "(SELECT MAX(publish_time) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as last_reply_time " +
            "FROM DiscussionTopic dt " +
            "LEFT JOIN Teacher t ON dt.author_id = t.teacher_id " +
            "LEFT JOIN Student s ON dt.author_id = s.student_id " +
            "WHERE dt.class_id IN (SELECT class_id FROM TeachingClass WHERE teacher_id = #{teacherId}) " +
            "ORDER BY dt.top_order DESC, dt.publish_time DESC")
    List<DiscussionTopic> selectByTeacherId(@Param("teacherId") String teacherId);

    // 搜索话题
    @Select("<script>" +
            "SELECT dt.*, " +
            "CASE WHEN t.name IS NOT NULL THEN t.name ELSE s.name END as author_name, " +
            "CASE WHEN t.name IS NOT NULL THEN 'teacher' ELSE 'student' END as role, " +
            "(SELECT COUNT(*) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as reply_count, " +
            "(SELECT MAX(publish_time) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as last_reply_time " +
            "FROM DiscussionTopic dt " +
            "LEFT JOIN Teacher t ON dt.author_id = t.teacher_id " +
            "LEFT JOIN Student s ON dt.author_id = s.student_id " +
            "WHERE dt.class_id IN (SELECT class_id FROM TeachingClass WHERE teacher_id = #{teacherId}) " +
            "<if test='classId != null and classId != \"\"'>" +
            "  AND dt.class_id = #{classId}" +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (dt.topic_title LIKE CONCAT('%', #{keyword}, '%') OR dt.topic_content LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY dt.top_order DESC, dt.publish_time DESC" +
            "</script>")
    List<DiscussionTopic> searchTopics(@Param("teacherId") String teacherId,
                                       @Param("classId") String classId,
                                       @Param("keyword") String keyword);

    // 获取话题详情
    @Select("SELECT dt.*, " +
            "CASE WHEN t.name IS NOT NULL THEN t.name ELSE s.name END as author_name, " +
            "CASE WHEN t.name IS NOT NULL THEN 'teacher' ELSE 'student' END as role, " +
            "(SELECT COUNT(*) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as reply_count, " +
            "(SELECT MAX(publish_time) FROM DiscussionReply dr WHERE dr.topic_id = dt.topic_id) as last_reply_time " +
            "FROM DiscussionTopic dt " +
            "LEFT JOIN Teacher t ON dt.author_id = t.teacher_id " +
            "LEFT JOIN Student s ON dt.author_id = s.student_id " +
            "WHERE dt.topic_id = #{topicId}")
    DiscussionTopic selectTopicDetail(@Param("topicId") String topicId);
}