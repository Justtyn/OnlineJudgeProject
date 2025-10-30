package com.example.onlinejudge.utils;

import com.example.onlinejudge.common.AccessLogProperties;
import com.example.onlinejudge.entity.AccessLog;
import com.example.onlinejudge.service.AccessLogWriter;
import com.example.onlinejudge.service.IpGeoService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {
    @Resource
    private AccessLogWriter writer;
    @Resource
    private AccessLogProperties props;
    @Resource
    private IpGeoService ipGeoService;

    private static final String START_TIME = "accessLogStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!props.isEnabled()) return true;
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!props.isEnabled()) return;
        Long start = (Long) request.getAttribute(START_TIME);
        if (start == null) return;

        try {
            long now = System.currentTimeMillis();
            int duration = (int) Math.max(0, now - start);

            String method = request.getMethod();
            String uriStr = request.getRequestURL().toString();
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) uriStr = uriStr + "?" + query;
            URI uri = URI.create(uriStr);

            String path = request.getRequestURI();

            String clientIp = IpUtil.getClientIp(request);
            String clientIps = IpUtil.getClientIps(request);
            String clientIpSrc = IpUtil.getClientIpSrc(request);

            AccessLog log = new AccessLog();
            log.setRequestTime(new Date(start));
            log.setResponseTime(new Date(now));
            log.setDurationMs(duration);

            log.setMethod(method);
            log.setScheme(uri.getScheme());
            log.setHost(uri.getHost());
            log.setPort(uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort());
            log.setUri(uriStr);
            log.setPath(path);
            log.setQueryString(request.getQueryString());

            log.setHttpStatus(response.getStatus());
            log.setReferer(request.getHeader("Referer"));
            log.setUserAgent(request.getHeader("User-Agent"));

            log.setClientIp(clientIp);
            log.setClientIpSrc(clientIpSrc);
            log.setClientIps(clientIps);

            // 归属地解析
            try {
                java.util.Map<String,String> geo = ipGeoService.resolve(clientIp);
                log.setIpCountry(geo.get("country"));
                log.setIpRegion(geo.get("region"));
                log.setIpCity(geo.get("city"));
                log.setIpIsp(geo.get("isp"));
            } catch (Exception ignore) { }

            // 业务用户信息：从JWT subject中取用户名（若有），此处保守填充
            try {
                String auth = request.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7);
                    String username = JwtUtil.parseToken(token).getSubject();
                    log.setUsername(username);
                }
            } catch (Exception ignore) { }

            // trace/request id
            String requestId = request.getHeader("X-Request-Id");
            if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
            log.setRequestId(requestId);

            boolean isAlert = false;
            String reason = null;
            if (duration > props.getSlowThresholdMs()) {
                isAlert = true;
                reason = "slow";
            }
            if (response.getStatus() >= 500) {
                isAlert = true;
                reason = (reason == null) ? "5xx" : reason + "/5xx";
            }
            log.setIsAlert(isAlert);
            log.setAlertReason(reason);

            writer.offer(log);
        } catch (Exception ignore) {
        }
    }
}


