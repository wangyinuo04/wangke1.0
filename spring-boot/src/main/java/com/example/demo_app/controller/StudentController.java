package com.example.demo_app.controller;

import com.example.demo_app.entity.Student;
import com.example.demo_app.entity.StudentCourseVO;
import com.example.demo_app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // ================== 学生端个人功能区域 ==================

    /**
     * 获取当前登录学生的课程列表
     */
    @GetMapping("/my-courses")
    public Map<String, Object> getMyCourses(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 从 Session 获取当前登录学生
        Student currentStudent = (Student) session.getAttribute("user");

        if (currentStudent == null) {
            result.put("success", false);
            result.put("message", "登录已过期，请重新登录");
            return result;
        }

        try {
            // 调用 Service 获取 VO 列表 (请确保 Service 中已实现该方法)
            List<StudentCourseVO> courses = studentService.getMyCourses(currentStudent.getStudentId());
            result.put("success", true);
            result.put("data", courses);
            result.put("message", "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取课程失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 使用邀请码加入班级
     */
    @PostMapping("/join-class")
    public Map<String, Object> joinClass(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String inviteCode = params.get("invitationCode");

        Student currentStudent = (Student) session.getAttribute("user");
        if (currentStudent == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            studentService.joinClass(currentStudent.getStudentId(), inviteCode);
            result.put("success", true);
            result.put("message", "加入班级成功！");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage()); // 返回 Service 抛出的具体错误信息
        }
        return result;
    }


    // ================== 管理员端功能区域 (保持原有代码不变) ==================

    /**
     * 获取所有学生列表（支持搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getStudentList(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Student> students = studentService.searchStudents(keyword);
            result.put("success", true);
            result.put("data", students);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取学生总数
     */
    @GetMapping("/count")
    public Map<String, Object> getStudentCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = studentService.count();
            result.put("success", true);
            result.put("data", count);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateStudent(@RequestBody Student student) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = studentService.updateStudentInfo(student);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "学生不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 切换账号状态
     */
    @PutMapping("/toggle-status/{studentId}")
    public Map<String, Object> toggleStatus(@PathVariable String studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = studentService.toggleStatus(studentId);
            if (success) {
                result.put("success", true);
                result.put("message", "操作成功");
            } else {
                result.put("success", false);
                result.put("message", "学生不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 重置密码
     */
    @PutMapping("/reset-password/{studentId}")
    public Map<String, Object> resetPassword(@PathVariable String studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = studentService.resetPassword(studentId);
            if (success) {
                result.put("success", true);
                result.put("message", "密码重置成功");
            } else {
                result.put("success", false);
                result.put("message", "学生不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "重置失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/delete/{studentId}")
    public Map<String, Object> deleteStudent(@PathVariable String studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = studentService.deleteStudent(studentId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "学生不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}