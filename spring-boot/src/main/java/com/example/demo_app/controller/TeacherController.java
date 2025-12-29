package com.example.demo_app.controller;

import com.example.demo_app.entity.Teacher;
import com.example.demo_app.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 获取所有教师列表（支持搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getTeacherList(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Teacher> teachers = teacherService.searchTeachers(keyword);
            result.put("success", true);
            result.put("data", teachers);
            result.put("message", "查询成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取教师总数
     */
    @GetMapping("/count")
    public Map<String, Object> getTeacherCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = teacherService.count();
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
     * 新增教师账户
     */
    @PostMapping("/add")
    public Map<String, Object> addTeacher(@RequestBody Map<String, Object> requestData, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求数据中获取教师信息
            Teacher teacher = new Teacher();
            teacher.setTeacherId((String) requestData.get("teacherId"));
            teacher.setName((String) requestData.get("name"));
            teacher.setGender((String) requestData.get("gender"));
            teacher.setDepartment((String) requestData.get("department"));
            teacher.setTitle((String) requestData.get("title"));
            teacher.setPhone((String) requestData.get("phone"));
            teacher.setEmail((String) requestData.get("email"));

            // 从请求头中获取当前管理员账号
            String adminAccount = httpRequest.getHeader("X-Admin-Account");

            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                // 如果没有从请求头获取到，从请求参数中获取
                adminAccount = (String) requestData.get("adminAccount");
            }

            // 如果还是没有获取到，使用默认值（确保数据库中有这个管理员账号）
            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                adminAccount = "admin001"; // 默认管理员账号，确保数据库中有这个账号
                System.out.println("警告：使用默认管理员账号：" + adminAccount);
            }

            System.out.println("当前操作的管理员账号: " + adminAccount);

            boolean success = teacherService.addTeacher(teacher, adminAccount);
            if (success) {
                result.put("success", true);
                result.put("message", "新增教师成功");
            } else {
                result.put("success", false);
                result.put("message", "教工号已存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "新增失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新教师信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateTeacher(@RequestBody Teacher teacher) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teacherService.updateTeacherInfo(teacher);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "教师不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 切换账号状态
     */
    @PutMapping("/toggle-status/{teacherId}")
    public Map<String, Object> toggleStatus(@PathVariable String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teacherService.toggleStatus(teacherId);
            if (success) {
                result.put("success", true);
                result.put("message", "操作成功");
            } else {
                result.put("success", false);
                result.put("message", "教师不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 重置密码
     */
    @PutMapping("/reset-password/{teacherId}")
    public Map<String, Object> resetPassword(@PathVariable String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teacherService.resetPassword(teacherId);
            if (success) {
                result.put("success", true);
                result.put("message", "密码重置成功");
            } else {
                result.put("success", false);
                result.put("message", "教师不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/delete/{teacherId}")
    public Map<String, Object> deleteTeacher(@PathVariable String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teacherService.deleteTeacher(teacherId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "教师不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}