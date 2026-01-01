package com.example.demo_app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo_app.entity.Student;
import com.example.demo_app.entity.StudentCourseVO;

import java.util.List;

/**
 * 学生服务接口
 */
public interface StudentService extends IService<Student> {

    // ========== 原有管理员功能 ==========

    /**
     * 搜索学生
     */
    List<Student> searchStudents(String keyword);

    /**
     * 根据行政班查询
     */
    List<Student> getStudentsByAdminClass(String adminClass);

    /**
     * 更新信息
     */
    boolean updateStudentInfo(Student student);

    /**
     * 切换状态
     */
    boolean toggleStatus(String studentId);

    /**
     * 重置密码
     */
    boolean resetPassword(String studentId);

    /**
     * 删除学生
     */
    boolean deleteStudent(String studentId);


    // ========== 新增学生端功能 ==========

    /**
     * 获取我的课程
     */
    List<StudentCourseVO> getMyCourses(String studentId);

    /**
     * 加入班级
     */
    void joinClass(String studentId, String invitationCode) throws Exception;
}