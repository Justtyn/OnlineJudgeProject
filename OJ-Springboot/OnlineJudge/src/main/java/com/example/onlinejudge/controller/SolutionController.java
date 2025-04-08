package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Solution;
import com.example.onlinejudge.service.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/solution")
public class SolutionController {
    
    @Autowired
    private SolutionService solutionService;
    
    /**
     * 添加题解
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addSolution(@RequestBody Solution solution) {
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
    
    /**
     * 删除题解
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteSolution(@PathVariable Integer id) {
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
    
    /**
     * 分页查询题解
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getSolutionPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
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
    
    /**
     * 根据ID查询题解
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSolutionById(@PathVariable Integer id) {
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
    
    /**
     * 增加题解点赞数
     */
    @PutMapping("/like/{id}")
    public ResponseEntity<Map<String, Object>> increaseLike(@PathVariable Integer id) {
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
}
