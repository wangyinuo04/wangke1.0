package com.example.demo_app.controller;

import com.example.demo_app.entity.Student;
import com.example.demo_app.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
// 不要加 @CrossOrigin 注解
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        System.out.println("收到登录请求: " + params);

        String role = params.get("role");
        String username = params.get("username");
        String password = params.get("password");

        Map<String, Object> result = loginService.login(role, username, password);
        System.out.println("登录结果: " + result);

        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Student student) {
        System.out.println("收到注册请求: " + student);
        return loginService.register(student);
    }

    /**
     * 管理员修改密码接口
     */
    @PostMapping("/admin/change-password")
    public Map<String, Object> changeAdminPassword(@RequestBody Map<String, String> params) {
        System.out.println("收到管理员密码修改请求: " + params);

        String account = params.get("account");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (account == null || account.trim().isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "账号不能为空");
            return errorResult;
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "旧密码不能为空");
            return errorResult;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "新密码不能为空");
            return errorResult;
        }

        if (newPassword.length() < 6) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "新密码长度不能少于6位");
            return errorResult;
        }

        Map<String, Object> result = loginService.changeAdminPassword(account, oldPassword, newPassword);
        System.out.println("管理员密码修改结果: " + result);

        return result;
    }

    @GetMapping("/test")
    public String test() {
        return "后端服务正常运行！";
    }
}