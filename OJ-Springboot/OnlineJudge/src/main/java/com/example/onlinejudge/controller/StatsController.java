package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.service.StatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 统计数据 REST 接口
 */
@Api(tags = "统计数据管理接口")
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Resource
    private StatsService statsService;

    @ApiOperation("获取用户注册趋势")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "days", value = "统计天数", defaultValue = "30", dataType = "int")
    })
    @GetMapping("/user/registration-trend")
    public Result registrationTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> data =
                statsService.getRegistrationTrend(days);
        return Result.success(data);
    }

    @ApiOperation("获取班级分布统计")
    @GetMapping("/user/class-distribution")
    public Result classDistribution() {
        List<Map<String, Object>> data =
                statsService.getClassDistribution();
        return Result.success(data);
    }

    @ApiOperation("获取题目提交排行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "limit", value = "获取数量", defaultValue = "10", dataType = "int")
    })
    @GetMapping("/problem/top-submit")
    public Result topProblemSubmit(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data =
                statsService.getTopProblemsBySubmit(limit);
        return Result.success(data);
    }

    @ApiOperation("获取提交状态分布")
    @GetMapping("/status/distribution")
    public Result statusDistribution() {
        List<Map<String, Object>> data =
                statsService.getStatusDistribution();
        return Result.success(data);
    }
}
