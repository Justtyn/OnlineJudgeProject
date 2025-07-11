package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Solution;
import com.example.onlinejudge.service.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "题解管理接口")
@RestController
@RequestMapping("/solution")
public class SolutionController {
    
    @Autowired
    private SolutionService solutionService;
    
    @ApiOperation("添加题解")
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addSolution(@ApiParam("题解信息") @RequestBody Solution solution) {
        Map<String, Object> result = new HashMap<>();
        boolean success = solutionService.addSolution(solution);
        
        if (success) {
            result.put("code", 200);
            result.put("message", "添加题解成功");
            result.put("data", solution);
        } else {
            result.put("code", 500);
            result.put("message", "添加题解失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("删除题解")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteSolution(@ApiParam("题解ID") @PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = solutionService.deleteSolution(id);
        
        if (success) {
            result.put("code", 200);
            result.put("message", "删除题解成功");
        } else {
            result.put("code", 500);
            result.put("message", "删除题解失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("分页查询题解")
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getSolutionPage(
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        Page<Solution> page = solutionService.getSolutionPage(current, size);
        
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", page);
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("根据ID查询题解")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSolutionById(@ApiParam("题解ID") @PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        Solution solution = solutionService.getSolutionById(id);
        
        if (solution != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", solution);
        } else {
            result.put("code", 404);
            result.put("message", "题解不存在");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("增加题解点赞数")
    @PutMapping("/like/{id}")
    public ResponseEntity<Map<String, Object>> increaseLike(@ApiParam("题解ID") @PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = solutionService.increaseLike(id);
        
        if (success) {
            result.put("code", 200);
            result.put("message", "点赞成功");
            // 返回更新后的题解信息
            Solution solution = solutionService.getSolutionById(id);
            result.put("data", solution);
        } else {
            result.put("code", 500);
            result.put("message", "点赞失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("根据用户ID查询题解")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSolutionsByUserId(
            @ApiParam("用户ID") @PathVariable Integer userId,
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        Page<Solution> page = solutionService.getSolutionsByUserId(userId, current, size);
        
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取题解总数")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getSolutionCount() {
        Map<String, Object> result = new HashMap<>();
        long count = solutionService.getSolutionCount();
        
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", count);
        
        return ResponseEntity.ok(result);
    }
}
