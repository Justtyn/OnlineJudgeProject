package com.example.onlinejudge.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "access-log")
public class AccessLogProperties {
    private boolean enabled = true;
    private int sampleRate = 100;
    private int slowThresholdMs = 2000;
    private boolean recordRequestBody = false;
    private boolean recordResponseBody = false;
    private List<String> ignorePathPrefixes;
}


