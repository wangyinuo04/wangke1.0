package com.example.demo_app.controller;

import com.example.demo_app.entity.Student;
import com.example.demo_app.entity.Teacher;
import com.example.demo_app.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/retrieve")
public class RetrievePasswordController {

    @Autowired
    private LoginService loginService;

    // 发送验证码 - 修改为根据用户ID自动获取邮箱
    @PostMapping("/sendCode")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String role = params.get("role");

        // 获取用户邮箱
        Map<String, Object> userResult = loginService.getUserEmail(username, role);
        if (!userResult.get("success").equals(true)) {
            return userResult;
        }

        String email = (String) userResult.get("email");
        String type = "reset_password";

        return loginService.sendCode(email, type);
    }

    // 验证验证码 - 修改为根据用户ID自动获取邮箱
    @PostMapping("/verifyCode")
    public Map<String, Object> verifyCode(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String role = params.get("role");
        String code = params.get("code");

        // 获取用户邮箱
        Map<String, Object> userResult = loginService.getUserEmail(username, role);
        if (!userResult.get("success").equals(true)) {
            return userResult;
        }

        String email = (String) userResult.get("email");
        String type = "reset_password";

        return loginService.verifyCode(email, code, type);
    }

    // 重置密码
    @PostMapping("/resetPassword")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String newPassword = params.get("newPassword");
        String role = params.get("role");

        return loginService.resetPassword(username, newPassword, role);
    }

    // 检查用户是否存在
    @PostMapping("/checkUser")
    public Map<String, Object> checkUser(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String role = params.get("role");

        Map<String, Object> result = loginService.getUserEmail(username, role);

        if (result.get("success").equals(true)) {
            result.put("message", "用户验证成功");
        }

        return result;
    }
}