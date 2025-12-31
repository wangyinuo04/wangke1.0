package com.example.demo_app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo_app.entity.Course;
import com.example.demo_app.entity.Teacher;
import com.example.demo_app.entity.TeachingClass;
import com.example.demo_app.mapper.CourseMapper;
import com.example.demo_app.mapper.TeachingClassMapper;
import com.example.demo_app.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeachingClassMapper teachingClassMapper;  // 注意：变量名是 teachingClassMapper（小写）

    @Autowired
    private CourseMapper courseMapper;  // 注意：变量名是 courseMapper（小写）

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

    /**
     * 获取教师教授的课程（从教学班中提取）
     */
    @GetMapping("/courses")
    public Map<String, Object> getTeacherCourses(@RequestParam String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询教师的教学班
            LambdaQueryWrapper<TeachingClass> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeachingClass::getTeacherId, teacherId);
            List<TeachingClass> teachingClasses = teachingClassMapper.selectList(wrapper);

            // 从教学班中提取课程信息
            List<Map<String, Object>> courses = new ArrayList<>();
            Set<String> courseIds = new HashSet<>();

            for (TeachingClass tc : teachingClasses) {
                if (tc.getCourseId() != null && !courseIds.contains(tc.getCourseId())) {
                    courseIds.add(tc.getCourseId());
                    // 查询课程详情
                    Course course = courseMapper.selectById(tc.getCourseId());
                    if (course != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("courseId", course.getCourseId());
                        courseInfo.put("courseName", course.getCourseName());
                        courses.add(courseInfo);
                    }
                }
            }

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
     * 获取当前登录教师信息
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentTeacher(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 方案1: 从请求头获取（前端需在axios拦截器中设置）
            String teacherId = request.getHeader("X-User-Id");

            // 方案2: 从session获取（如果使用session登录）
            // String teacherId = (String) request.getSession().getAttribute("userId");

            // 方案3: 如果以上都没有，尝试从JWT token解析
            if (teacherId == null || teacherId.trim().isEmpty()) {
                // 从Authorization头解析JWT
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // TODO: 解析JWT token获取userId
                    // String token = authHeader.substring(7);
                    // teacherId = JwtUtil.parseToken(token);
                }
            }

            // 如果还是获取不到，返回错误
            if (teacherId == null || teacherId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "未获取到用户身份信息，请重新登录");
                return result;
            }

            Teacher teacher = teacherService.getById(teacherId);
            if (teacher != null) {
                result.put("success", true);
                result.put("data", teacher);
                result.put("message", "查询成功");
            } else {
                result.put("success", false);
                result.put("message", "教师不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新当前教师的联系方式
     */
    @PutMapping("/update-contact")
    public Map<String, Object> updateContactInfo(@RequestBody Map<String, String> contactInfo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取当前登录教师ID
            String teacherId = request.getHeader("X-User-Id");
            if (teacherId == null || teacherId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "未获取到用户身份信息");
                return result;
            }

            Teacher teacher = teacherService.getById(teacherId);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师不存在");
                return result;
            }

            // 更新联系方式
            teacher.setPhone(contactInfo.get("phone"));
            teacher.setEmail(contactInfo.get("email"));

            boolean success = teacherService.updateById(teacher);
            if (success) {
                result.put("success", true);
                result.put("message", "联系方式更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 修改密码（需要验证原密码）
     */
    @PutMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> passwordInfo, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取当前登录教师ID
            String teacherId = request.getHeader("X-User-Id");
            if (teacherId == null || teacherId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "未获取到用户身份信息");
                return result;
            }

            String oldPassword = passwordInfo.get("oldPassword");
            String newPassword = passwordInfo.get("newPassword");
            String confirmPassword = passwordInfo.get("confirmPassword");

            // 验证新密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "两次输入的新密码不一致");
                return result;
            }

            // 调用服务层修改密码
            boolean success = teacherService.changePassword(teacherId, oldPassword, newPassword);
            if (success) {
                result.put("success", true);
                result.put("message", "密码修改成功，请重新登录");
            } else {
                result.put("success", false);
                result.put("message", "原密码错误或修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "修改失败：" + e.getMessage());
        }
        return result;
    }
}