package com.example.onlinejudge.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AccessLog {
    private Long id;

    private Date requestTime;
    private Date responseTime;
    private Integer durationMs;

    private String method;
    private String scheme;
    private String host;
    private Integer port;
    private String uri;
    private String path;
    private String queryString;
    private String routePattern;

    private Integer httpStatus;
    private String referer;
    private String userAgent;

    private String clientIp;
    private String clientIpSrc;
    private String clientIps;

    private Long userId;
    private String userType; // student/teacher/admin
    private String username;
    private String role;

    private String headers; // store JSON string if needed
    private String requestBody;
    private String responseBody;

    private String traceId;
    private String requestId;
    private String app;
    private String env;
    private String instance;

    private String ipCountry;
    private String ipRegion;
    private String ipCity;
    private String ipIsp;

    private Boolean isAlert;
    private String alertReason;

    private Date createdAt;
    private Date updatedAt;
}


