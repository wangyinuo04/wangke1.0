package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.*;
import com.example.demo_app.mapper.*;
// import com.example.demo_app.service.StudentService; // 如果都在同一个包下，这行不需要；如果在impl包下，需要取消注释
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private TeachingClassMapper teachingClassMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    // ==========================================
    //Part 1: 原有管理员端逻辑 (从旧文件迁移过来)
    // ==========================================

    @Override
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

    @Override
    public List<Student> getStudentsByAdminClass(String adminClass) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getClassName, adminClass);
        return list(wrapper);
    }

    @Override
    public boolean updateStudentInfo(Student student) {
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

    @Override
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

    @Override
    public boolean resetPassword(String studentId) {
        Student student = getById(studentId);
        if (student == null) {
            return false;
        }
        student.setLoginPassword("123456");
        return updateById(student);
    }

    @Override
    public boolean deleteStudent(String studentId) {
        return removeById(studentId);
    }

    // ==========================================
    // Part 2: 新增学生端逻辑 (新编写的代码)
    // ==========================================

    @Override
    public List<StudentCourseVO> getMyCourses(String studentId) {
        // 1. 查询选课记录
        QueryWrapper<Enrollment> enrollQuery = new QueryWrapper<>();
        enrollQuery.eq("student_id", studentId);
        List<Enrollment> enrollments = enrollmentMapper.selectList(enrollQuery);

        List<StudentCourseVO> resultList = new ArrayList<>();

        // 2. 组装 VO
        for (Enrollment enroll : enrollments) {
            TeachingClass tc = teachingClassMapper.selectById(enroll.getClassId());
            if (tc != null) {
                Course course = courseMapper.selectById(tc.getCourseId());
                Teacher teacher = teacherMapper.selectById(tc.getTeacherId());

                StudentCourseVO vo = new StudentCourseVO();
                vo.setClassId(tc.getClassId());
                vo.setCourseId(course.getCourseId());
                vo.setCourseName(course.getCourseName());
                vo.setClassName(tc.getClassName());
                vo.setSemester(tc.getSemester());
                vo.setCredit(course.getCredit());
                vo.setTeacherName(teacher != null ? teacher.getName() : "未知教师");
                vo.setStatus("进行中");

                resultList.add(vo);
            }
        }
        return resultList;
    }

    @Override
    public void joinClass(String studentId, String invitationCode) throws Exception {
        // 1. 查找班级
        QueryWrapper<TeachingClass> classQuery = new QueryWrapper<>();
        classQuery.eq("invitation_code", invitationCode);
        TeachingClass teachingClass = teachingClassMapper.selectOne(classQuery);

        if (teachingClass == null) {
            throw new Exception("邀请码无效，找不到对应的班级");
        }

        // 2. 检查是否重复加入
        QueryWrapper<Enrollment> checkQuery = new QueryWrapper<>();
        checkQuery.eq("student_id", studentId).eq("class_id", teachingClass.getClassId());
        if (enrollmentMapper.selectCount(checkQuery) > 0) {
            throw new Exception("你已经加入了该课程，请勿重复加入");
        }

        // 3. 插入记录
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setClassId(teachingClass.getClassId());
        enrollment.setJoinTime(LocalDateTime.now());
        enrollmentMapper.insert(enrollment);
    }
}