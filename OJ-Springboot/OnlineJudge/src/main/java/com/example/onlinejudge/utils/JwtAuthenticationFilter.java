package com.example.onlinejudge.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 登录接口不进行 Token 校验（或其他白名单接口）
        String path = request.getRequestURI();
        if ("/api/login".equals(path) || "/api/register".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中获取 Token，要求格式为 "Bearer token字符串"
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 当 Token 缺失或格式不正确时，可以直接抛出异常或设置响应状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token缺失或格式不正确");
            return;
        }
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

        try {
            // 尝试解析 Token，如果不合法会抛出异常
            JwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token已过期");
            return;
        } catch (SignatureException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token验证失败");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token解析错误");
            return;
        }
        // 如果 Token 验证成功，则继续调用下一个过滤器或目标处理器
        filterChain.doFilter(request, response);
    }
}
