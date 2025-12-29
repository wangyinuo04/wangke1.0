package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Course;
import com.example.demo_app.entity.Enrollment;
import com.example.demo_app.entity.Teacher;
import com.example.demo_app.entity.TeachingClass;
import com.example.demo_app.mapper.EnrollmentMapper;
import com.example.demo_app.mapper.TeachingClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TeachingClassService extends ServiceImpl<TeachingClassMapper, TeachingClass> {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private EnrollmentMapper enrollmentMapper;  // 使用 Mapper 而不是 Service

    private final AtomicInteger counter = new AtomicInteger(1000);

    /**
     * 搜索教学班
     */
    public List<TeachingClass> searchTeachingClasses(String keyword) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(TeachingClass::getClassName, keyword)
                    .or()
                    .like(TeachingClass::getClassId, keyword);
        }
        wrapper.orderByDesc(TeachingClass::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 开设教学班
     */
    @Transactional
    public boolean addTeachingClass(TeachingClass teachingClass, String adminAccount) {
        // 验证课程是否存在
        Course course = courseService.getById(teachingClass.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 验证教师是否存在
        Teacher teacher = teacherService.getById(teachingClass.getTeacherId());
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }

        // 生成唯一的class_id
        String classId = this.generateClassId();

        // 检查class_id是否已存在
        int attempts = 0;
        while (getById(classId) != null && attempts < 10) {
            classId = this.generateClassId();
            attempts++;
        }

        if (getById(classId) != null) {
            throw new RuntimeException("生成班级ID失败，请稍后重试");
        }

        teachingClass.setClassId(classId);

        // 生成邀请码（6位随机码）
        String invitationCode = this.generateInvitationCode();
        teachingClass.setInvitationCode(invitationCode);

        // 设置管理员账号
        teachingClass.setAdminAccount(adminAccount);

        // 设置创建时间
        teachingClass.setCreatedTime(LocalDateTime.now());

        // 设置邀请码过期时间（默认30天后）
        teachingClass.setExpiryDate(LocalDateTime.now().plusDays(30));

        return save(teachingClass);
    }

    /**
     * 生成唯一的 class_id
     */
    private String generateClassId() {
        // 格式: TC + 时间戳(后8位) + 计数器
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        String lastDigits = timestampStr.substring(timestampStr.length() - 8);
        int counterValue = counter.incrementAndGet() % 10000;
        return "TC" + lastDigits + String.format("%04d", counterValue);
    }

    /**
     * 生成邀请码
     */
    private String generateInvitationCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 6).toUpperCase();
    }

    /**
     * 更新教学班信息
     */
    @Transactional
    public boolean updateTeachingClassInfo(TeachingClass teachingClass) {
        TeachingClass existing = getById(teachingClass.getClassId());
        if (existing == null) {
            return false;
        }

        // 只更新允许修改的字段
        existing.setSemester(teachingClass.getSemester());
        existing.setClassName(teachingClass.getClassName());
        existing.setMaxStudents(teachingClass.getMaxStudents());
        existing.setTeacherId(teachingClass.getTeacherId());

        return updateById(existing);
    }

    /**
     * 删除教学班
     */
    @Transactional
    public boolean deleteTeachingClass(String classId) {
        try {
            // 1. 先删除与该教学班相关的所有选课记录
            QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", classId);
            enrollmentMapper.delete(queryWrapper);

            // 2. 删除教学班本身
            return removeById(classId);
        } catch (Exception e) {
            throw new RuntimeException("删除教学班失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取教学班详情
     */
    public TeachingClass getTeachingClassDetail(String classId) {
        return getById(classId);
    }

    /**
     * 检查班级是否存在
     */
    public boolean existsById(String classId) {
        return getById(classId) != null;
    }

    /**
     * 获取教师的所有教学班
     */
    public List<TeachingClass> getTeachingClassesByTeacher(String teacherId) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingClass::getTeacherId, teacherId);
        wrapper.orderByDesc(TeachingClass::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 获取课程的所有教学班
     */
    public List<TeachingClass> getTeachingClassesByCourse(String courseId) {
        LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingClass::getCourseId, courseId);
        wrapper.orderByDesc(TeachingClass::getCreatedTime);
        return list(wrapper);
    }

    /**
     * 根据班级ID获取最大学生数
     */
    public Integer getMaxStudentsByClassId(String classId) {
        TeachingClass teachingClass = getById(classId);
        return teachingClass != null ? teachingClass.getMaxStudents() : null;
    }
}