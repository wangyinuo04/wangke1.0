package com.example.demo_app.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    /**
     * 使用 Filter 方式处理跨域，优先级最高
     * 解决 OPTIONS 请求被拦截的问题
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名 (Spring Boot 2.4+ 使用 allowedOriginPatterns)
        config.addAllowedOriginPattern("*");
        // 允许携带 Cookie (关键!)
        config.setAllowCredentials(true);
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有方法 (GET, POST, PUT, DELETE, OPTIONS)
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}