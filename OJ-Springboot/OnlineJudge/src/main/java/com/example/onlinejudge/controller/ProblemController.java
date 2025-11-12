package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Problem;
import com.example.onlinejudge.service.ProblemService;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "问题管理接口")
@RestController
@RequestMapping("/problem")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @ApiOperation("分页查询所有问题")
    @GetMapping("/page")
    public Result page(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectPage(pageNum, pageSize);
        return Result.success(resultMap);
    }

    @ApiOperation("根据ID查询问题详情")
    @GetMapping("/{id}")
    public Result getById(@ApiParam(value = "问题ID", required = true) @PathVariable Integer id) {
        Problem problem = problemService.selectById(id);
        return Result.success(problem);
    }

    @ApiOperation("根据名称查询问题")
    @GetMapping("/name")
    public Result getByName(
            @ApiParam(value = "问题名称", required = true) @RequestParam String name,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectByName(name, pageNum, pageSize);
        return Result.success(resultMap);
    }

    @ApiOperation("根据出题人查询问题")
    @GetMapping("/setter")
    public Result getBySetter(
            @ApiParam(value = "出题人名称", required = true) @RequestParam String setter,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectBySetter(setter, pageNum, pageSize);
        return Result.success(resultMap);
    }

    @ApiOperation("添加问题")
    @PostMapping
    public Result add(@ApiParam(value = "问题对象", required = true) @RequestBody Problem problem) {
        problemService.add(problem);
        return Result.success("添加成功");
    }

    @ApiOperation("修改问题")
    @PutMapping
    public Result update(@ApiParam(value = "问题对象", required = true) @RequestBody Problem problem) {
        problemService.update(problem);
        return Result.success("修改成功");
    }

    @ApiOperation("删除问题")
    @DeleteMapping("/{id}")
    public Result delete(@ApiParam(value = "问题ID", required = true) @PathVariable Integer id) {
        problemService.deleteById(id);
        return Result.success("删除成功");
    }

    @ApiOperation("增加问题提交次数")
    @PutMapping("/{id}/submit")
    public Result incrementSubmitCount(@ApiParam(value = "问题ID", required = true) @PathVariable Integer id) {
        problemService.incrementSubmitCount(id);
        return Result.success("提交次数更新成功");
    }

    @ApiOperation("增加问题通过次数")
    @PutMapping("/{id}/ac")
    public Result incrementAcCount(@ApiParam(value = "问题ID", required = true) @PathVariable Integer id) {
        problemService.incrementAcCount(id);
        return Result.success("通过次数更新成功");
    }

    @ApiOperation("查询所有问题")
    @GetMapping("/all")
    public Result getAllProblems() {
        List<Problem> problems = problemService.selectAll();
        return Result.success(problems);
    }

    @ApiOperation("查询所有问题的简要信息")
    @GetMapping("/simple")
    public Result getAllProblemsSimple() {
        List<Problem> problems = problemService.selectAllSimple();
        return Result.success(problems);
    }

    @ApiOperation("获取每日挑战题目")
    @GetMapping("/daily-challenge")
    public Result getDailyChallenge() {
        List<Problem> problems = problemService.getDailyChallenge();
        return Result.success(problems);
    }

    @ApiOperation("根据关键词搜索题目名称和内容")
    @GetMapping("/search")
    public Result searchByKeyword(
            @ApiParam(value = "搜索关键词", required = true) @RequestParam String keyword) {
        List<Problem> problems = problemService.searchByKeyword(keyword);
        return Result.success(problems);
    }
} 