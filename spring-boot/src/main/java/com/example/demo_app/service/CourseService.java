package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Course;
import com.example.demo_app.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    /**
     * 搜索课程（按课程名称或代码）
     */
    public List<Course> searchCourses(String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getCourseId, keyword);
        }
        wrapper.orderByDesc(Course::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 新增课程
     */
    public boolean addCourse(Course course, String adminAccount) {
        // 检查课程代码是否已存在
        if (getById(course.getCourseId()) != null) {
            return false;
        }

        // 设置默认值
        course.setAdminAccount(adminAccount);
        course.setCreatedTime(LocalDateTime.now());

        return save(course);
    }

    /**
     * 更新课程信息
     */
    public boolean updateCourseInfo(Course course) {
        // 检查课程是否存在
        Course existing = getById(course.getCourseId());
        if (existing == null) {
            return false;
        }

        // 更新允许修改的字段
        existing.setCourseName(course.getCourseName());
        existing.setCredit(course.getCredit());
        existing.setCollege(course.getCollege());
        existing.setCourseType(course.getCourseType());
        existing.setDescription(course.getDescription());

        return updateById(existing);
    }

    /**
     * 删除课程
     */
    public boolean deleteCourse(String courseId) {
        return removeById(courseId);
    }
}