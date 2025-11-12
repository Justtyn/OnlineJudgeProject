package com.example.onlinejudge.utils;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * 用于注册和配置应用中的各种过滤器
 * Spring Boot会自动扫描并加载该配置类
 */
@Configuration
public class FilterConfig {

    /**
     * 创建并注册JWT认证过滤器
     *
     * @return 配置好的JWT过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        // 创建过滤器注册Bean，用于向Spring容器注册过滤器
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

        // 设置要注册的JWT认证过滤器实例
        registrationBean.setFilter(new JwtAuthenticationFilter());

        // 配置过滤器的URL匹配模式，使其对所有API请求生效
        // 注意：登录接口在过滤器内部做了放行处理
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
