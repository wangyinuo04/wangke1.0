package com.example.demo_app.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("*") // 允许所有来源 (Spring Boot 2.4+ 使用 allowedOriginPatterns)
                // 或者明确指定前端地址，这样更安全： .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowCredentials(true) // <=== 关键！允许携带 Cookie
                .maxAge(3600) // 预检请求缓存时间
                .allowedHeaders("*");
    }
}