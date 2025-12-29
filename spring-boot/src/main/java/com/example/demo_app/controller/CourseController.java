package com.example.demo_app.controller;

import com.example.demo_app.entity.Course;
import com.example.demo_app.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取所有课程列表（支持搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getCourseList(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Course> courses = courseService.searchCourses(keyword);
            result.put("success", true);
            result.put("data", courses);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取课程总数
     */
    @GetMapping("/count")
    public Map<String, Object> getCourseCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = courseService.count();
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
     * 新增课程
     */
    @PostMapping("/add")
    public Map<String, Object> addCourse(@RequestBody Map<String, Object> requestData, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求数据中获取课程信息
            Course course = new Course();
            course.setCourseId((String) requestData.get("courseId"));
            course.setCourseName((String) requestData.get("courseName"));
            course.setCredit(Float.parseFloat(requestData.get("credit").toString()));
            course.setCollege((String) requestData.get("college"));
            course.setCourseType((String) requestData.get("courseType"));
            course.setDescription((String) requestData.get("description"));

            // 从请求头中获取当前管理员账号
            String adminAccount = httpRequest.getHeader("X-Admin-Account");

            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                // 如果没有从请求头获取到，从请求参数中获取
                adminAccount = (String) requestData.get("adminAccount");
            }

            // 如果还是没有获取到，使用默认值
            if (adminAccount == null || adminAccount.trim().isEmpty()) {
                adminAccount = "admin001";
                System.out.println("警告：使用默认管理员账号：" + adminAccount);
            }

            System.out.println("当前操作的管理员账号: " + adminAccount);

            boolean success = courseService.addCourse(course, adminAccount);
            if (success) {
                result.put("success", true);
                result.put("message", "新增课程成功");
            } else {
                result.put("success", false);
                result.put("message", "课程代码已存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "新增失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新课程信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateCourse(@RequestBody Course course) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = courseService.updateCourseInfo(course);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "课程不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{courseId}")
    public Map<String, Object> deleteCourse(@PathVariable String courseId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = courseService.deleteCourse(courseId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "课程不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}