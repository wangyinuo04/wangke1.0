package com.example.demo_app.controller;

import com.example.demo_app.entity.Student;
import com.example.demo_app.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
// 不要加 @CrossOrigin，使用全局 WebConfig 配置
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params, HttpSession session) {
        System.out.println("收到登录请求: " + params);

        String role = params.get("role");
        String username = params.get("username");
        String password = params.get("password");

        Map<String, Object> result = loginService.login(role, username, password);
        System.out.println("登录结果: " + result);

        // ================== 核心修正点 ==================
        if (Boolean.TRUE.equals(result.get("success"))) {
            // 修正：从 "userInfo" 字段获取用户对象，而不是 "data"
            Object user = result.get("userInfo");

            if (user != null) {
                // 写入 Session
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(60 * 60); // 1小时过期

                System.out.println(">>> 登录成功！Session ID: " + session.getId());
                System.out.println(">>> Session 已写入用户: " + user);
            } else {
                System.err.println(">>> 警告：登录成功但未获取到 userInfo 对象，Session 写入失败！");
            }
        }
        // ==============================================

        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Student student) {
        return loginService.register(student);
    }

    @PostMapping("/admin/change-password")
    public Map<String, Object> changeAdminPassword(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (account == null || oldPassword == null || newPassword == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "参数不完整");
            return err;
        }
        return loginService.changeAdminPassword(account, oldPassword, newPassword);
    }

    @GetMapping("/test")
    public String test() {
        return "后端服务正常运行！";
    }
}