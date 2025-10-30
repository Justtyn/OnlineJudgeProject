package com.example.onlinejudge.service;

import com.example.onlinejudge.entity.AccessLog;
import com.example.onlinejudge.mapper.AccessLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessLogService {
    @Resource
    private AccessLogMapper accessLogMapper;

    public Map<String, Object> page(java.util.Date from, java.util.Date to,
                                    String method, String pathLike, Integer status,
                                    Integer minDuration, Integer maxDuration,
                                    String clientIp, String username, Boolean isAlert,
                                    Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo == null ? 1 : pageNo) - 1) * (pageSize == null ? 20 : pageSize);
        int limit = pageSize == null ? 20 : pageSize;
        long total = accessLogMapper.countByFilter(from, to, method, pathLike, status, minDuration, maxDuration, clientIp, username, isAlert);
        List<AccessLog> list = accessLogMapper.selectByFilter(from, to, method, pathLike, status, minDuration, maxDuration, clientIp, username, isAlert, offset, limit);
        Map<String, Object> res = new HashMap<>();
        res.put("total", total);
        res.put("records", list);
        return res;
    }
}


