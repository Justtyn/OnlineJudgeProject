package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatusService extends IService<Status> {
    // 获取总提交数
    long count();

    // 获取提交状态分布
    List<Map<String, Object>> getStatusDistribution();

    // 获取编程语言使用分布
    List<Map<String, Object>> getLanguageDistribution();

    // 获取每日提交趋势
    List<Map<String, Object>> getDailySubmissionTrend();

    // 获取指定时间段内的提交次数
    long getSubmissionCount(LocalDate startDate, LocalDate endDate);
} 