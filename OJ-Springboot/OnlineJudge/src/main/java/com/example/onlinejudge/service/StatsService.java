package com.example.onlinejudge.service;

import java.util.List;
import java.util.Map;

/**
 * 统计业务接口
 */
public interface StatsService {
    List<Map<String, Object>> getRegistrationTrend(int days);

    List<Map<String, Object>> getClassDistribution();

    List<Map<String, Object>> getTopProblemsBySubmit(int limit);

    List<Map<String, Object>> getStatusDistribution();
}
