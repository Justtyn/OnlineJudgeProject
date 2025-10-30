package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.AccessLog;
import com.example.onlinejudge.service.AccessLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access-log")
public class AccessLogController {
    @Resource
    private AccessLogService accessLogService;

    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date from,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date to,
            String method, String pathLike, Integer status,
            Integer minDuration, Integer maxDuration,
            String clientIp, String username, Boolean isAlert,
            Integer pageNo, Integer pageSize) {
        return Result.success(accessLogService.page(from, to, method, pathLike, status, minDuration, maxDuration, clientIp, username, isAlert, pageNo, pageSize));
    }

    @GetMapping("/export")
    public void export(
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date from,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date to,
            String method, String pathLike, Integer status,
            Integer minDuration, Integer maxDuration,
            String clientIp, String username, Boolean isAlert,
            HttpServletResponse response) throws Exception {
        // 简化：导出最多前 5000 条
        Map<String, Object> data = accessLogService.page(from, to, method, pathLike, status, minDuration, maxDuration, clientIp, username, isAlert, 1, 5000);
        @SuppressWarnings("unchecked")
        List<AccessLog> list = (List<AccessLog>) data.get("records");

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("access_log.csv", "UTF-8"));
        PrintWriter writer = response.getWriter();
        writer.println("request_time,response_time,duration_ms,method,path,http_status,client_ip,username,is_alert,alert_reason");
        for (AccessLog l : list) {
            writer.print(s(l.getRequestTime())); writer.print(',');
            writer.print(s(l.getResponseTime())); writer.print(',');
            writer.print(n(l.getDurationMs())); writer.print(',');
            writer.print(s(l.getMethod())); writer.print(',');
            writer.print(s(l.getPath())); writer.print(',');
            writer.print(n(l.getHttpStatus())); writer.print(',');
            writer.print(s(l.getClientIp())); writer.print(',');
            writer.print(s(l.getUsername())); writer.print(',');
            writer.print(b(l.getIsAlert())); writer.print(',');
            writer.print(s(l.getAlertReason()));
            writer.println();
        }
        writer.flush();
    }

    private static String s(Object o) { return o == null ? "" : String.valueOf(o).replace("\n"," ").replace("\r"," "); }
    private static String n(Number n) { return n == null ? "" : String.valueOf(n); }
    private static String b(Boolean b) { return b == null ? "" : (b ? "1" : "0"); }
}


