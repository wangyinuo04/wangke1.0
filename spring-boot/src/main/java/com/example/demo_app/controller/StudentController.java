package com.example.demo_app.controller;

import com.example.demo_app.entity.Student;
import com.example.demo_app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class StudentController {

    @Autowired
    private StudentService studentService;

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