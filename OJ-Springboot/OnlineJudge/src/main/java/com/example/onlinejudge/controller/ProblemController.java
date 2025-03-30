package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Problem;
import com.example.onlinejudge.service.ProblemService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/problem")
public class ProblemController {
    
    @Resource
    private ProblemService problemService;
    
    /**
     * 分页查询所有问题
     * 通过分页参数获取问题列表
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页数量，默认为 10
     * @return 返回查询结果，封装在 Result 对象中
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectPage(pageNum, pageSize);
        return Result.success(resultMap);
    }
    
    /**
     * 根据ID查询问题详情
     * 根据问题的唯一 ID 查询具体问题信息
     * @param id 问题的唯一标识符
     * @return 返回查询的单个问题，封装在 Result 对象中
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Problem problem = problemService.selectById(id);
        return Result.success(problem);
    }
    
    /**
     * 根据名称查询问题
     * 根据问题名称查询相关问题，支持分页
     * @param name 问题的名称
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页数量，默认为 10
     * @return 返回查询结果，封装在 Result 对象中
     */
    @GetMapping("/name")
    public Result getByName(@RequestParam String name,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectByName(name, pageNum, pageSize);
        return Result.success(resultMap);
    }
    
    /**
     * 根据出题人查询问题
     * 根据出题人名称查询相关问题，支持分页
     * @param setter 出题人的名称
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页数量，默认为 10
     * @return 返回查询结果，封装在 Result 对象中
     */
    @GetMapping("/setter")
    public Result getBySetter(@RequestParam String setter,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectBySetter(setter, pageNum, pageSize);
        return Result.success(resultMap);
    }
    
    /**
     * 添加问题
     * 用于添加新的问题到数据库
     * @param problem 请求体中的问题对象
     * @return 返回添加成功的结果
     */
    @PostMapping
    public Result add(@RequestBody Problem problem) {
        problemService.add(problem);
        return Result.success("添加成功");
    }
    
    /**
     * 修改问题
     * 用于修改已存在的问题
     * @param problem 请求体中的问题对象，包含修改后的数据
     * @return 返回修改成功的结果
     */
    @PutMapping
    public Result update(@RequestBody Problem problem) {
        problemService.update(problem);
        return Result.success("修改成功");
    }
    
    /**
     * 删除问题
     * 根据问题的 ID 删除问题
     * @param id 问题的唯一标识符
     * @return 返回删除成功的结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        problemService.deleteById(id);
        return Result.success("删除成功");
    }
    
    /**
     * 增加问题提交次数
     * @param id 问题的唯一标识符
     * @return 返回更新成功的结果
     */
    @PutMapping("/{id}/submit")
    public Result incrementSubmitCount(@PathVariable Integer id) {
        problemService.incrementSubmitCount(id);
        return Result.success("提交次数更新成功");
    }
    
    /**
     * 增加问题通过次数
     * @param id 问题的唯一标识符
     * @return 返回更新成功的结果
     */
    @PutMapping("/{id}/ac")
    public Result incrementAcCount(@PathVariable Integer id) {
        problemService.incrementAcCount(id);
        return Result.success("通过次数更新成功");
    }
} 