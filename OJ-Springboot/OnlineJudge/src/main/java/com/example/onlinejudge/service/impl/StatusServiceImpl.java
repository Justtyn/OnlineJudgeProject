package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Status;
import com.example.onlinejudge.mapper.StatusMapper;
import com.example.onlinejudge.service.StatusService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class StatusServiceImpl extends ServiceImpl<StatusMapper, Status> implements StatusService {
    @Resource
    private StatusMapper statusMapper;

    @Override
    public long count() {
        return statusMapper.selectCount(null);
    }

    @Override
    public List<Map<String, Object>> getStatusDistribution() {
        return statusMapper.selectStatusDistribution();
    }

    @Override
    public List<Map<String, Object>> getLanguageDistribution() {
        return statusMapper.selectLanguageDistribution();
    }

    @Override
    public List<Map<String, Object>> getDailySubmissionTrend() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return statusMapper.selectDailySubmissionTrend(thirtyDaysAgo);
    }

    @Override
    public long getSubmissionCount(LocalDate startDate, LocalDate endDate) {
        return statusMapper.countSubmissions(startDate, endDate);
    }
} 