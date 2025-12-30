package com.example.demo_app.controller;

import com.example.demo_app.entity.Course;
import com.example.demo_app.entity.Student;
import com.example.demo_app.entity.TeachingClass;
import com.example.demo_app.mapper.EnrollmentMapper;
import com.example.demo_app.service.CourseService;
import com.example.demo_app.service.EnrollmentService;
import com.example.demo_app.service.StudentService;
import com.example.demo_app.service.TeachingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/teachingClass")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class TeachingClassController {

    @Autowired
    private TeachingClassService teachingClassService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    /**
     * 获取教师负责的所有教学班（包含学生人数）
     */
    @GetMapping("/teacher/{teacherId}")
    public Map<String, Object> getTeachingClassesByTeacher(@PathVariable String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取教师的所有教学班
            List<TeachingClass> teachingClasses = teachingClassService.getTeachingClassesByTeacher(teacherId);

            // 为每个教学班添加学生人数统计和课程名称
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (TeachingClass tc : teachingClasses) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("id", tc.getClassId()); // 前端需要id字段
                classInfo.put("semester", tc.getSemester());
                classInfo.put("className", tc.getClassName());
                classInfo.put("invitationCode", tc.getInvitationCode());
                classInfo.put("expiryDate", tc.getExpiryDate());

                // 获取课程名称
                Course course = courseService.getById(tc.getCourseId());
                if (course != null) {
                    classInfo.put("courseName", course.getCourseName());
                } else {
                    classInfo.put("courseName", "未知课程");
                }

                // 统计学生人数 - 重要：确保字段名正确
                long studentCount = enrollmentMapper.countByClassId(tc.getClassId());
                classInfo.put("studentCount", studentCount); // 这个字段名必须匹配前端

                // 获取学生列表
                List<String> studentIds = enrollmentMapper.findStudentIdsByClassId(tc.getClassId());
                List<Map<String, String>> students = new ArrayList<>();
                for (String studentId : studentIds) {
                    Student student = studentService.getById(studentId);
                    if (student != null) {
                        Map<String, String> stu = new HashMap<>();
                        stu.put("id", student.getStudentId());
                        stu.put("name", student.getName());
                        stu.put("adminClass", student.getClassName());
                        students.add(stu);
                    }
                }
                classInfo.put("students", students);

                resultList.add(classInfo);
            }

            result.put("success", true);
            result.put("data", resultList);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 生成或更新班级邀请码
     */
    @PostMapping("/{classId}/generate-invite-code")
    public Map<String, Object> generateInviteCode(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TeachingClass teachingClass = teachingClassService.getById(classId);
            if (teachingClass == null) {
                result.put("success", false);
                result.put("message", "教学班不存在");
                return result;
            }

            // 生成6位随机邀请码（字母和数字）
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            StringBuilder code = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }

            // 设置有效期（7天后）
            LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

            // 更新数据库
            teachingClass.setInvitationCode(code.toString());
            teachingClass.setExpiryDate(expiryDate);
            boolean success = teachingClassService.updateById(teachingClass);

            if (success) {
                // 改为传统的HashMap方式
                Map<String, Object> data = new HashMap<>();
                data.put("inviteCode", code.toString());
                data.put("expiryDate", expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                result.put("success", true);
                result.put("data", data);
                result.put("message", "邀请码生成成功");
            } else {
                result.put("success", false);
                result.put("message", "生成邀请码失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取班级学生详细信息列表
     */
    @GetMapping("/{classId}/student-details")
    public Map<String, Object> getClassStudentDetails(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TeachingClass teachingClass = teachingClassService.getById(classId);
            if (teachingClass == null) {
                result.put("success", false);
                result.put("message", "教学班不存在");
                return result;
            }

            List<String> studentIds = enrollmentMapper.findStudentIdsByClassId(classId);
            List<Map<String, String>> students = new ArrayList<>();

            for (String studentId : studentIds) {
                Student student = studentService.getById(studentId);
                if (student != null) {
                    Map<String, String> stu = new HashMap<>();
                    stu.put("id", student.getStudentId());
                    stu.put("name", student.getName());
                    stu.put("adminClass", student.getClassName());
                    students.add(stu);
                }
            }

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
     * 获取所有教学班列表（支持搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getTeachingClassList(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<TeachingClass> classes = teachingClassService.searchTeachingClasses(keyword);
            result.put("success", true);
            result.put("data", classes);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取教学班总数
     */
    @GetMapping("/count")
    public Map<String, Object> getTeachingClassCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = teachingClassService.count();
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
     * 新增教学班
     */
    @PostMapping("/add")
    public Map<String, Object> addTeachingClass(@RequestBody Map<String, Object> requestData, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求数据中获取教学班信息
            TeachingClass teachingClass = new TeachingClass();
            teachingClass.setSemester((String) requestData.get("semester"));
            teachingClass.setClassName((String) requestData.get("className"));
            teachingClass.setMaxStudents(Integer.parseInt(requestData.get("maxStudents").toString()));
            teachingClass.setTeacherId((String) requestData.get("teacherId"));
            teachingClass.setCourseId((String) requestData.get("courseId"));

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

            boolean success = teachingClassService.addTeachingClass(teachingClass, adminAccount);
            if (success) {
                result.put("success", true);
                result.put("data", teachingClass.getClassId()); // 返回生成的class_id
                result.put("message", "开设教学班成功");
            } else {
                result.put("success", false);
                result.put("message", "开设教学班失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "开设失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新教学班信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateTeachingClass(@RequestBody TeachingClass teachingClass) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teachingClassService.updateTeachingClassInfo(teachingClass);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "教学班不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除教学班
     */
    @DeleteMapping("/delete/{classId}")
    public Map<String, Object> deleteTeachingClass(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = teachingClassService.deleteTeachingClass(classId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "教学班不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 将单个学生加入教学班
     */
    @PostMapping("/enroll/single")
    public Map<String, Object> enrollSingleStudent(@RequestBody Map<String, String> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String classId = requestData.get("classId");
            String studentId = requestData.get("studentId");

            boolean success = enrollmentService.enrollSingleStudent(classId, studentId);
            if (success) {
                result.put("success", true);
                result.put("message", "添加学生成功");
            } else {
                result.put("success", false);
                result.put("message", "添加学生失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 批量将学生加入教学班
     */
    @PostMapping("/enroll/batch")
    public Map<String, Object> enrollBatchStudents(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String classId = (String) requestData.get("classId");
            @SuppressWarnings("unchecked")
            List<String> studentIds = (List<String>) requestData.get("studentIds");

            boolean success = enrollmentService.enrollBatchStudents(classId, studentIds);
            if (success) {
                result.put("success", true);
                result.put("message", "批量添加成功，共添加 " + studentIds.size() + " 名学生");
            } else {
                result.put("success", false);
                result.put("message", "批量添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 从教学班移除学生
     */
    @DeleteMapping("/remove-student")
    public Map<String, Object> removeStudent(@RequestBody Map<String, String> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String classId = requestData.get("classId");
            String studentId = requestData.get("studentId");

            boolean success = enrollmentService.removeStudentFromClass(classId, studentId);
            if (success) {
                result.put("success", true);
                result.put("message", "移除学生成功");
            } else {
                result.put("success", false);
                result.put("message", "移除学生失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取教学班的学生列表
     */
    @GetMapping("/{classId}/students")
    public Map<String, Object> getClassStudents(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> studentIds = enrollmentService.getClassStudentIds(classId);
            result.put("success", true);
            result.put("data", studentIds);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据行政班级查询学生
     */
    @GetMapping("/search-students")
    public Map<String, Object> searchStudentsByAdminClass(@RequestParam String adminClass) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Student> students = enrollmentService.getStudentsByAdminClass(adminClass);
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
     * 获取教学班详情
     */
    @GetMapping("/detail/{classId}")
    public Map<String, Object> getTeachingClassDetail(@PathVariable String classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TeachingClass teachingClass = teachingClassService.getTeachingClassDetail(classId);
            if (teachingClass != null) {
                result.put("success", true);
                result.put("data", teachingClass);
                result.put("message", "查询成功");
            } else {
                result.put("success", false);
                result.put("message", "教学班不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}