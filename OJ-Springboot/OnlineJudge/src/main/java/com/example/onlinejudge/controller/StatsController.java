package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.service.StatsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 统计数据 REST 接口
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Resource
    private StatsService statsService;

    /** 1. 用户注册趋势（折线图数据） */
    @GetMapping("/user/registration-trend")
    public Result registrationTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> data =
                statsService.getRegistrationTrend(days);
        return Result.success(data);
    }

    /** 2. 班级分布（饼图/柱状图数据） */
    @GetMapping("/user/class-distribution")
    public Result classDistribution() {
        List<Map<String, Object>> data =
                statsService.getClassDistribution();
        return Result.success(data);
    }

    /** 3. 题目提交排行（柱状图数据） */
    @GetMapping("/problem/top-submit")
    public Result topProblemSubmit(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data =
                statsService.getTopProblemsBySubmit(limit);
        return Result.success(data);
    }

    /** 4. 提交状态分布（饼图数据） */
    @GetMapping("/status/distribution")
    public Result statusDistribution() {
        List<Map<String, Object>> data =
                statsService.getStatusDistribution();
        return Result.success(data);
    }
}
