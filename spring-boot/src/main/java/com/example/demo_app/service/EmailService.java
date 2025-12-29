package com.example.demo_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.regex.Pattern;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 邮箱验证正则
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * 清理邮箱地址
     */
    private String cleanEmailAddress(String email) {
        if (email == null) return null;
        // 移除所有不可见字符和控制字符
        return email.trim().replaceAll("[\\x00-\\x1F\\x7F]", "").replaceAll("#.*", "");
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String cleanEmail = cleanEmailAddress(email);
        return EMAIL_PATTERN.matcher(cleanEmail).matches();
    }

    /**
     * 发送密码重置验证码邮件
     */
    public boolean sendPasswordResetCode(String toEmail, String code) {
        String cleanEmail = cleanEmailAddress(toEmail);

        if (!isValidEmail(cleanEmail)) {
            System.err.println("无效的邮箱地址: " + toEmail + " -> 清理后: " + cleanEmail);
            return false;
        }

        String subject = "今课堂 - 密码重置验证码";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #1890ff;'>今课堂密码重置</h2>" +
                "<p>您好！</p>" +
                "<p>您正在申请重置密码，验证码为：</p>" +
                "<div style='background: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<h1 style='color: #1890ff; margin: 0; text-align: center;'>" + code + "</h1>" +
                "</div>" +
                "<p>验证码有效期为5分钟，请尽快使用。</p>" +
                "<p>如果这不是您本人操作，请忽略此邮件。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";

        return sendHtmlEmail(cleanEmail, subject, htmlContent);
    }

    /**
     * 发送HTML格式邮件
     */
    private boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置发件人，使用InternetAddress避免编码问题
            helper.setFrom(new InternetAddress(fromEmail, "今课堂系统"));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("发送邮件失败 - 收件人: " + toEmail + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}