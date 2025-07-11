package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Status;
import com.example.onlinejudge.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "提交状态管理接口")
@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    // 添加内部DTO类
    private static class StatusDTO {
        private final Integer problemId;
        private final String status;
        private final String submitTime;
        
        public StatusDTO(Status status) {
            this.problemId = status.getProblemId();
            this.status = status.getStatus();
            this.submitTime = status.getCreatTime();
        }

        public Integer getProblemId() {
            return problemId;
        }

        public String getStatus() {
            return status;
        }

        public String getSubmitTime() {
            return submitTime;
        }
    }

    @ApiOperation("添加提交状态")
    @PostMapping
    public Result<?> save(@ApiParam("提交状态信息") @RequestBody Status status) {
        statusService.save(status);
        return Result.success();
    }

    @ApiOperation("删除提交状态")
    @DeleteMapping("/{id}")
    public Result<?> delete(@ApiParam("状态ID") @PathVariable Integer id) {
        statusService.removeById(id);
        return Result.success();
    }

    @ApiOperation("分页查询提交状态")
    @GetMapping("/page")
    public Result<?> findPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("creat_time");
        Page<Status> statusPage = new Page<>(pageNum, pageSize);
        Page<Status> page = statusService.page(statusPage, queryWrapper);
        return Result.success(page);
    }

    @ApiOperation("根据ID查询提交状态")
    @GetMapping("/{id}")
    public Result<?> findById(@ApiParam("状态ID") @PathVariable Integer id) {
        Status status = statusService.getById(id);
        return Result.success(status);
    }

    @ApiOperation("根据用户ID查询提交状态")
    @GetMapping("/user/{userId}")
    public Result<?> findByUserId(
            @ApiParam("用户ID") @PathVariable Integer userId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("creat_time");
        Page<Status> page = statusService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

    @ApiOperation("根据题目ID查询提交状态")
    @GetMapping("/problem/{problemId}")
    public Result<?> findByProblemId(
            @ApiParam("题目ID") @PathVariable Integer problemId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId)
                   .orderByDesc("creat_time");
        Page<Status> page = statusService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

    @ApiOperation("查询用户每日挑战题目完成状态")
    @GetMapping("/user/{userId}/daily")
    public Result<?> getDailyChallengeStatus(
            @ApiParam("用户ID") @PathVariable Integer userId,
            @ApiParam("日期") @RequestParam String date) {
        LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        LocalDate nextDay = targetDate.plusDays(1);
        
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .ge("creat_time", targetDate.toString())
                   .lt("creat_time", nextDay.toString())
                   .eq("status", "Accepted");
        
        List<Status> statusList = statusService.list(queryWrapper);
        List<StatusDTO> result = statusList.stream()
                .map(StatusDTO::new)
                .collect(Collectors.toList());
        
        return Result.success(result);
    }
} 