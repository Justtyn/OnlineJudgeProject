package com.example.onlinejudge.service.impl;

import com.example.onlinejudge.mapper.StatsMapper;
import com.example.onlinejudge.service.StatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 统计业务实现
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private StatsMapper statsMapper;

    @Override
    public List<Map<String, Object>> getRegistrationTrend(int days) {
        return statsMapper.selectRegistrationTrend(days);
    }

    @Override
    public List<Map<String, Object>> getClassDistribution() {
        return statsMapper.selectClassDistribution();
    }

    @Override
    public List<Map<String, Object>> getTopProblemsBySubmit(int limit) {
        return statsMapper.selectTopProblemsBySubmit(limit);
    }

    @Override
    public List<Map<String, Object>> getStatusDistribution() {
        return statsMapper.selectStatusDistribution();
    }
}
