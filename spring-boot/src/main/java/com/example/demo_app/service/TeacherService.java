package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Course;
import com.example.demo_app.entity.Teacher;
import com.example.demo_app.entity.TeachingClass;
import com.example.demo_app.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherService extends ServiceImpl<TeacherMapper, Teacher> {

    /**
     * 新增教师账户
     */
    public boolean addTeacher(Teacher teacher, String adminAccount) {
        // 检查教工号是否已存在
        if (getById(teacher.getTeacherId()) != null) {
            return false;
        }

        // 设置默认值
        teacher.setAdminAccount(adminAccount);
        teacher.setCreatedTime(LocalDateTime.now());
        teacher.setAccountStatus("正常");

        // 初始密码默认设置为123456（前端会提示）
        // 这里可以加密处理，建议使用BCrypt加密
        teacher.setLoginPassword("123456"); // 实际项目中应加密

        return save(teacher);
    }

    /**
     * 搜索教师（按姓名或教工号）
     */
    public List<Teacher> searchTeachers(String keyword) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Teacher::getName, keyword)
                    .or()
                    .like(Teacher::getTeacherId, keyword);
        }
        wrapper.orderByDesc(Teacher::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 更新教师信息
     */
    public boolean updateTeacherInfo(Teacher teacher) {
        // 只允许更新部分字段（不能修改教工号、密码、状态等）
        Teacher existing = getById(teacher.getTeacherId());
        if (existing == null) {
            return false;
        }

        // 只更新允许修改的字段
        existing.setName(teacher.getName());
        existing.setGender(teacher.getGender());
        existing.setDepartment(teacher.getDepartment());
        existing.setTitle(teacher.getTitle());
        existing.setPhone(teacher.getPhone());
        existing.setEmail(teacher.getEmail());

        return updateById(existing);
    }

    /**
     * 切换账号状态（启用/禁用）
     */
    public boolean toggleStatus(String teacherId) {
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            return false;
        }

        if ("正常".equals(teacher.getAccountStatus())) {
            teacher.setAccountStatus("禁用");
        } else {
            teacher.setAccountStatus("正常");
        }

        return updateById(teacher);
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(String teacherId) {
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            return false;
        }

        // 重置为默认密码123456
        teacher.setLoginPassword("123456"); // 实际项目中应加密
        return updateById(teacher);
    }

    /**
     * 删除教师（物理删除 - 直接从数据库删除记录）
     */
    public boolean deleteTeacher(String teacherId) {
        // 直接从数据库删除记录
        return removeById(teacherId);
    }

    // 在TeacherService类中添加以下方法：

    /**
     * 获取教师教授的课程
     */
    public List<Course> getTeacherCourses(String teacherId) {
        // 这里需要实现根据教师ID获取其教授的课程
        // 由于需要Course实体类，先返回空列表
        return new ArrayList<>();
    }

    /**
     * 获取教师的教学班
     */
    public List<TeachingClass> getTeachingClassesByTeacher(String teacherId) {
        // 这里需要实现根据教师ID获取其教学班
        // 由于需要TeachingClass实体类，先返回空列表
        return new ArrayList<>();
    }
}