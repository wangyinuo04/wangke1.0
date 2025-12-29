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

    @GetMapping("/test")
    public String test() {
        return "后端服务正常运行！";
    }
}