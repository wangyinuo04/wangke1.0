package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Student;
import com.example.demo_app.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService extends ServiceImpl<StudentMapper, Student> {

    /**
     * 搜索学生（按学号或姓名）
     */
    public List<Student> searchStudents(String keyword) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Student::getStudentId, keyword)
                    .or()
                    .like(Student::getName, keyword)
                    .or()
                    .like(Student::getClassName, keyword);
        }
        return list(wrapper);
    }

    /**
     * 根据行政班级查询学生
     */
    public List<Student> getStudentsByAdminClass(String adminClass) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getClassName, adminClass);
        return list(wrapper);
    }

    /**
     * 更新学生信息
     */
    public boolean updateStudentInfo(Student student) {
        // 检查学生是否存在
        Student existing = getById(student.getStudentId());
        if (existing == null) {
            return false;
        }

        // 更新所有允许修改的字段
        existing.setName(student.getName());
        existing.setGender(student.getGender());
        existing.setDepartment(student.getDepartment());
        existing.setClassName(student.getClassName());
        existing.setMajor(student.getMajor());
        existing.setEnrollmentYear(student.getEnrollmentYear());
        existing.setPhone(student.getPhone());
        existing.setEmail(student.getEmail());

        return updateById(existing);
    }

    /**
     * 切换账号状态（启用/禁用）
     */
    public boolean toggleStatus(String studentId) {
        Student student = getById(studentId);
        if (student == null) {
            return false;
        }

        if ("正常".equals(student.getAccountStatus())) {
            student.setAccountStatus("禁用");
        } else {
            student.setAccountStatus("正常");
        }

        return updateById(student);
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(String studentId) {
        Student student = getById(studentId);
        if (student == null) {
            return false;
        }

        // 重置为默认密码123456
        student.setLoginPassword("123456");
        return updateById(student);
    }

    /**
     * 删除学生（物理删除）
     */
    public boolean deleteStudent(String studentId) {
        return removeById(studentId);
    }
}