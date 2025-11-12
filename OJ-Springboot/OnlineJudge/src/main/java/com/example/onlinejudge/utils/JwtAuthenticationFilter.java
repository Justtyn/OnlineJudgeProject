package com.example.onlinejudge.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于拦截HTTP请求，验证JWT令牌的有效性
 * 继承自OncePerRequestFilter确保每个请求只会被过滤一次
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 如果请求方法是 OPTIONS，则直接放行
        // OPTIONS请求通常是预检请求(preflight request)，用于CORS跨域资源共享
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 登录接口不进行 Token 校验（或其他白名单接口）
        // 对于登录和注册接口，用户尚未获取token，因此不需要进行token验证
        String path = request.getRequestURI();
        if ("/login".equals(path) || "/register".equals(path) ||
                "/api/student/resetPassword".equals(path) || "/api/student/changePassword".equals(path) ||
                "/teacher/resetPassword".equals(path) || "/teacher/changePassword".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 静态资源路径不需要token验证
        if (path.startsWith("/uploads/") || path.startsWith("/static/") ||
                path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") ||
                path.endsWith(".gif") || path.endsWith(".ico") || path.endsWith(".css") ||
                path.endsWith(".js") || path.endsWith(".html")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中获取 Token，要求格式为 "Bearer token字符串"
        // Authorization头是HTTP标准头部，用于携带认证信息
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 如果Authorization头不存在或格式不正确，返回401未授权状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token缺失或格式不正确");
            return;
        }
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀，提取实际token值

        try {
            // 尝试解析token，验证其有效性
            // 如果token有效，JwtUtil.parseToken会成功执行并返回Claims对象
            JwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            // 捕获token过期异常，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token已过期");
            return;
        } catch (SignatureException e) {
            // 捕获签名验证失败异常，可能是token被篡改
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token验证失败");
            return;
        } catch (Exception e) {
            // 捕获其他所有异常，如格式错误等
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token解析错误");
            return;
        }

        // token验证通过，继续执行过滤器链中的下一个过滤器或处理请求
        filterChain.doFilter(request, response);
    }
}
