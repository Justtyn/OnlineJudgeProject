package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Status;
import com.example.onlinejudge.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    // 添加状态
    @PostMapping
    public Result<?> save(@RequestBody Status status) {
        statusService.save(status);
        return Result.success();
    }

    // 删除状态
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        statusService.removeById(id);
        return Result.success();
    }

    // 分页查询全部状态
    @GetMapping("/page")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        Page<Status> statusPage = new Page<>(pageNum, pageSize);
        Page<Status> page = statusService.page(statusPage, queryWrapper);
        return Result.success(page);
    }

    // 按id查询
    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Integer id) {
        Status status = statusService.getById(id);
        return Result.success(status);
    }

    // 按user_id查询
    @GetMapping("/user/{userId}")
    public Result<?> findByUserId(@PathVariable Integer userId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Page<Status> page = statusService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

    // 按problem_id查询
    @GetMapping("/problem/{problemId}")
    public Result<?> findByProblemId(@PathVariable Integer problemId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Status> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId);
        Page<Status> page = statusService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }
} 