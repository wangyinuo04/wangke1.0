package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    @Select("SELECT h.*, tc.class_name, " +
            "(SELECT COUNT(*) FROM HomeworkSubmission hs WHERE hs.homework_id = h.homework_id) as submitted_count, " +
            "(SELECT COUNT(*) FROM Enrollment e WHERE e.class_id = h.class_id) as total_count " +
            "FROM Homework h " +
            "LEFT JOIN TeachingClass tc ON h.class_id = tc.class_id " +
            "WHERE h.class_id IN (SELECT class_id FROM TeachingClass WHERE teacher_id = #{teacherId}) " +
            "ORDER BY h.publish_time DESC")
    List<Homework> getHomeworkListByTeacher(@Param("teacherId") String teacherId);

    @Select("SELECT * FROM Homework WHERE class_id = #{classId} ORDER BY publish_time DESC")
    List<Homework> getHomeworkByClass(@Param("classId") String classId);
}