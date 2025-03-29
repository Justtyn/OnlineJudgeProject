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
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> resultMap = problemService.selectPage(pageNum, pageSize);
        return Result.success(resultMap);
    }
    
    /**
     * 根据ID查询问题详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Problem problem = problemService.selectById(id);
        return Result.success(problem);
    }
    
    /**
     * 根据名称查询问题
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
     */
    @PostMapping
    public Result add(@RequestBody Problem problem) {
        problemService.add(problem);
        return Result.success("添加成功");
    }
    
    /**
     * 修改问题
     */
    @PutMapping
    public Result update(@RequestBody Problem problem) {
        problemService.update(problem);
        return Result.success("修改成功");
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        problemService.deleteById(id);
        return Result.success("删除成功");
    }
} 