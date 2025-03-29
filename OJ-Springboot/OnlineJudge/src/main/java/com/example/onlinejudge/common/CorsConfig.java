package com.example.onlinejudge.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 该配置类用于启用跨域资源共享 (CORS)，以允许不同源的请求访问该应用程序的接口。
 * CORS 是一种机制，通过该机制，浏览器允许网页向不同域（域名、协议或端口不同）的服务器发起请求。
 * 具体来说，它配置了允许的请求来源、请求方法和请求头。
 */
@Configuration // 声明该类为配置类，Spring 容器会扫描并加载它
public class CorsConfig {

    /**
     * 配置跨域过滤器
     * Spring 使用 CorsFilter 处理跨域请求
     * 该方法返回一个 CorsFilter Bean，供 Spring 容器管理
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建一个 URL 基于的跨域配置源，用于为不同 URL 路径提供不同的 CORS 配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 创建一个 CorsConfiguration 对象，配置跨域的具体规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 设置允许的请求来源，"*" 表示允许所有来源的请求
        corsConfiguration.addAllowedOrigin("*"); // 1 设置访问源地址

        // 设置允许的请求头，"*" 表示允许所有请求头
        corsConfiguration.addAllowedHeader("*"); // 2 设置访问源请求头

        // 设置允许的请求方法，"*" 表示允许所有 HTTP 方法（GET, POST, PUT, DELETE, etc.）
        corsConfiguration.addAllowedMethod("*"); // 3 设置访问源请求方法

        // 注册该配置并指定所有请求路径（"/**"）都使用该跨域配置
        source.registerCorsConfiguration("/**", corsConfiguration); // 4 对接口配置跨域设置

        // 创建并返回一个 CorsFilter，该过滤器会在每次请求时应用该跨域配置
        return new CorsFilter(source);
    }
}