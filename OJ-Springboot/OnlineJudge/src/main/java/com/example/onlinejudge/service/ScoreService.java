package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Course;

import java.util.Map;

public interface ScoreService extends IService<Course> {
    /**
     * 获取班级总数
     */
    long count();

    /**
     * 获取班级作业数量分布
     */
    Map<String, Long> getHomeworkCountDistribution();
} 