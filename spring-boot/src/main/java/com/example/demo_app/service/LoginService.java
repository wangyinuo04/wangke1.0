package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo_app.entity.Student;
import com.example.demo_app.entity.Teacher;
import com.example.demo_app.entity.Administrator;
import com.example.demo_app.entity.SmsCode;
import com.example.demo_app.mapper.StudentMapper;
import com.example.demo_app.mapper.TeacherMapper;
import com.example.demo_app.mapper.AdministratorMapper;
import com.example.demo_app.mapper.SmsCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class LoginService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Autowired
    private EmailService emailService;

    // 邮箱验证正则表达式
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Map<String, Object> login(String role, String username, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            switch (role) {
                case "student":
                    QueryWrapper<Student> studentQuery = new QueryWrapper<>();
                    studentQuery.eq("student_id", username);
                    Student student = studentMapper.selectOne(studentQuery);

                    if (student == null) {
                        result.put("success", false);
                        result.put("message", "学号不存在");
                    } else if (!student.getLoginPassword().equals(password)) {
                        result.put("success", false);
                        result.put("message", "密码错误");
                    } else if ("禁用".equals(student.getAccountStatus())) {
                        result.put("success", false);
                        result.put("message", "账号已被禁用");
                    } else {
                        result.put("success", true);
                        result.put("message", "登录成功");
                        result.put("userInfo", student);
                    }
                    break;

                case "teacher":
                    QueryWrapper<Teacher> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("teacher_id", username);
                    Teacher teacher = teacherMapper.selectOne(teacherQuery);

                    if (teacher == null) {
                        result.put("success", false);
                        result.put("message", "教工号不存在");
                    } else if (!teacher.getLoginPassword().equals(password)) {
                        result.put("success", false);
                        result.put("message", "密码错误");
                    } else if ("禁用".equals(teacher.getAccountStatus())) {
                        result.put("success", false);
                        result.put("message", "账号已被禁用");
                    } else {
                        result.put("success", true);
                        result.put("message", "登录成功");
                        result.put("userInfo", teacher);
                    }
                    break;

                case "admin":
                    QueryWrapper<Administrator> adminQuery = new QueryWrapper<>();
                    adminQuery.eq("account", username);
                    Administrator admin = administratorMapper.selectOne(adminQuery);

                    if (admin == null) {
                        result.put("success", false);
                        result.put("message", "管理员账号不存在");
                    } else if (!admin.getLoginPassword().equals(password)) {
                        result.put("success", false);
                        result.put("message", "密码错误");
                    } else {
                        result.put("success", true);
                        result.put("message", "登录成功");
                        result.put("userInfo", admin);
                    }
                    break;

                default:
                    result.put("success", false);
                    result.put("message", "角色错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> register(Student student) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查学号是否已存在
            QueryWrapper<Student> query = new QueryWrapper<>();
            query.eq("student_id", student.getStudentId());
            Student existing = studentMapper.selectOne(query);

            if (existing != null) {
                result.put("success", false);
                result.put("message", "学号已存在");
                return result;
            }

            // 设置默认值
            student.setAccountStatus("正常");

            int rows = studentMapper.insert(student);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "注册成功");
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }

        return result;
    }

    // 获取用户邮箱
    public Map<String, Object> getUserEmail(String username, String role) {
        Map<String, Object> result = new HashMap<>();

        try {
            String email = null;
            boolean exists = false;

            switch (role) {
                case "student":
                    QueryWrapper<Student> studentQuery = new QueryWrapper<>();
                    studentQuery.eq("student_id", username);
                    Student student = studentMapper.selectOne(studentQuery);
                    if (student != null) {
                        exists = true;
                        email = student.getEmail();
                    }
                    break;

                case "teacher":
                    QueryWrapper<Teacher> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("teacher_id", username);
                    Teacher teacher = teacherMapper.selectOne(teacherQuery);
                    if (teacher != null) {
                        exists = true;
                        email = teacher.getEmail();
                    }
                    break;

                default:
                    result.put("success", false);
                    result.put("message", "只支持学生和老师找回密码");
                    return result;
            }

            if (!exists) {
                result.put("success", false);
                result.put("message", role.equals("student") ? "学号不存在" : "教工号不存在");
            } else if (email == null || email.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该账号未绑定邮箱，请联系管理员");
            } else if (!isValidEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱格式不正确: " + email);
            } else {
                result.put("success", true);
                result.put("message", "用户验证成功");
                result.put("email", email.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    // 发送验证码
    public Map<String, Object> sendCode(String email, String type) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证邮箱格式
            if (!isValidEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱格式不正确");
                return result;
            }

            // 检查1分钟内是否已发送过验证码
            int recentCount = smsCodeMapper.countRecentCodes(email, type);
            if (recentCount > 0) {
                result.put("success", false);
                result.put("message", "验证码发送过于频繁，请稍后再试");
                return result;
            }

            // 生成6位随机验证码
            String code = String.valueOf(new Random().nextInt(900000) + 100000);

            // 保存验证码到数据库
            SmsCode smsCode = new SmsCode();
            smsCode.setContact(email);
            smsCode.setCode(code);
            smsCode.setType(type);
            smsCode.setCreateTime(LocalDateTime.now());
            smsCode.setExpireTime(LocalDateTime.now().plusMinutes(5)); // 5分钟过期
            smsCode.setUsed(false);

            smsCodeMapper.insert(smsCode);

            // 调用邮件服务发送验证码
            boolean sent = emailService.sendPasswordResetCode(email, code);

            if (sent) {
                System.out.println("邮件验证码已发送到: " + email);
                result.put("success", true);
                result.put("message", "验证码已发送到您的邮箱: " + email);
            } else {
                // 如果发送失败，删除数据库中的验证码记录
                smsCodeMapper.deleteById(smsCode.getId());
                result.put("success", false);
                result.put("message", "邮件发送失败，请检查邮箱地址是否正确");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }

        return result;
    }

    // 验证验证码
    public Map<String, Object> verifyCode(String email, String code, String type) {
        Map<String, Object> result = new HashMap<>();

        try {
            SmsCode smsCode = smsCodeMapper.findValidCode(email, code, type);

            if (smsCode == null) {
                result.put("success", false);
                result.put("message", "验证码无效或已过期");
            } else {
                // 标记验证码为已使用
                smsCodeMapper.markAsUsed(smsCode.getId());
                result.put("success", true);
                result.put("message", "验证码验证成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    // 重置密码
    public Map<String, Object> resetPassword(String username, String newPassword, String role) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = 0;

            switch (role) {
                case "student":
                    Student student = new Student();
                    student.setStudentId(username);
                    student.setLoginPassword(newPassword);
                    rows = studentMapper.updateById(student);
                    break;

                case "teacher":
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(username);
                    teacher.setLoginPassword(newPassword);
                    rows = teacherMapper.updateById(teacher);
                    break;

                default:
                    result.put("success", false);
                    result.put("message", "角色错误");
                    return result;
            }

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "密码重置成功");
            } else {
                result.put("success", false);
                result.put("message", "密码重置失败，用户不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
        }

        return result;
    }

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // 清理字符串，移除不可见字符
        String cleanEmail = email.trim().replaceAll("[\\x00-\\x1F\\x7F]", "");
        return EMAIL_PATTERN.matcher(cleanEmail).matches();
    }

    /**
     * 管理员修改密码
     */
    public Map<String, Object> changeAdminPassword(String account, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证旧密码
            QueryWrapper<Administrator> adminQuery = new QueryWrapper<>();
            adminQuery.eq("account", account);
            Administrator admin = administratorMapper.selectOne(adminQuery);

            if (admin == null) {
                result.put("success", false);
                result.put("message", "管理员账号不存在");
                return result;
            }

            if (!admin.getLoginPassword().equals(oldPassword)) {
                result.put("success", false);
                result.put("message", "旧密码错误");
                return result;
            }

            if (oldPassword.equals(newPassword)) {
                result.put("success", false);
                result.put("message", "新密码不能与旧密码相同");
                return result;
            }

            // 更新密码
            admin.setLoginPassword(newPassword);
            int rows = administratorMapper.updateById(admin);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "密码修改成功");
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }

        return result;
    }
}