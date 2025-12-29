package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Enrollment;
import com.example.demo_app.entity.Student;
import com.example.demo_app.mapper.EnrollmentMapper;
import com.example.demo_app.mapper.TeachingClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService extends ServiceImpl<EnrollmentMapper, Enrollment> {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeachingClassMapper teachingClassMapper;

    /**
     * 将单个学生加入教学班
     */
    @Transactional
    public boolean enrollSingleStudent(String classId, String studentId) {
        // 检查教学班是否存在
        if (teachingClassMapper.selectById(classId) == null) {
            throw new RuntimeException("教学班不存在");
        }

        // 检查学生是否存在
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        // 检查是否已加入
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId)
                .eq("student_id", studentId);
        long existingCount = this.count(queryWrapper);
        if (existingCount > 0) {
            throw new RuntimeException("学生已在该教学班中");
        }

        // 检查班级人数是否已满
        long currentCount = this.countByClassId(classId);
        Integer maxStudents = teachingClassMapper.selectMaxStudentsByClassId(classId);
        if (maxStudents == null) {
            throw new RuntimeException("获取班级信息失败");
        }
        if (currentCount >= maxStudents) {
            throw new RuntimeException("教学班人数已满");
        }

        // 添加学生到教学班
        Enrollment enrollment = new Enrollment();
        enrollment.setClassId(classId);
        enrollment.setStudentId(studentId);
        enrollment.setJoinTime(LocalDateTime.now());

        return this.save(enrollment);
    }

    /**
     * 批量添加学生到教学班
     */
    @Transactional
    public boolean enrollBatchStudents(String classId, List<String> studentIds) {
        // 检查教学班是否存在
        if (teachingClassMapper.selectById(classId) == null) {
            throw new RuntimeException("教学班不存在");
        }

        // 检查班级当前人数
        long currentCount = this.countByClassId(classId);
        Integer maxStudents = teachingClassMapper.selectMaxStudentsByClassId(classId);
        if (maxStudents == null) {
            throw new RuntimeException("获取班级信息失败");
        }
        if (currentCount + studentIds.size() > maxStudents) {
            throw new RuntimeException("批量添加后班级人数将超过限制");
        }

        // 过滤掉已存在的关系
        List<String> existingStudentIds = this.findStudentIdsByClassId(classId);
        List<String> newStudentIds = studentIds.stream()
                .filter(id -> !existingStudentIds.contains(id))
                .collect(Collectors.toList());

        if (newStudentIds.isEmpty()) {
            throw new RuntimeException("所有学生都已在该教学班中");
        }

        // 批量插入
        List<Enrollment> enrollments = newStudentIds.stream().map(studentId -> {
            Enrollment enrollment = new Enrollment();
            enrollment.setClassId(classId);
            enrollment.setStudentId(studentId);
            enrollment.setJoinTime(LocalDateTime.now());
            return enrollment;
        }).collect(Collectors.toList());

        return this.saveBatch(enrollments);
    }

    /**
     * 从教学班移除学生
     */
    @Transactional
    public boolean removeStudentFromClass(String classId, String studentId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId)
                .eq("student_id", studentId);

        return this.remove(queryWrapper);
    }

    /**
     * 删除指定班级的所有选课记录
     */
    @Transactional
    public boolean removeAllByClassId(String classId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取教学班的所有学生ID
     */
    public List<String> getClassStudentIds(String classId) {
        return baseMapper.findStudentIdsByClassId(classId);
    }

    /**
     * 获取教学班的学生数量
     */
    public long getClassStudentCount(String classId) {
        return baseMapper.countByClassId(classId);
    }

    /**
     * 根据行政班级查询学生
     */
    public List<Student> getStudentsByAdminClass(String adminClass) {
        return studentService.getStudentsByAdminClass(adminClass);
    }

    /**
     * 检查学生是否已在教学班中
     */
    public boolean isStudentInClass(String studentId, String classId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId)
                .eq("student_id", studentId);

        return this.count(queryWrapper) > 0;
    }

    /**
     * 使用Mapper自定义查询方法获取学生ID列表
     */
    private List<String> findStudentIdsByClassId(String classId) {
        return baseMapper.findStudentIdsByClassId(classId);
    }

    /**
     * 使用Mapper自定义查询方法统计学生数量
     */
    private long countByClassId(String classId) {
        return baseMapper.countByClassId(classId);
    }
}