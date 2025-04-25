package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Homework;
import com.example.onlinejudge.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/homework")
public class HomeworkController {
    
    @Autowired
    private HomeworkService homeworkService;
    
    @PostMapping
    public Result<Boolean> addHomework(@RequestBody Homework homework) {
        boolean success = homeworkService.addHomework(homework);
        return success ? Result.success(true) : Result.error("500", "添加作业失败");
    }
    
    @DeleteMapping("/{id}")
    public boolean deleteHomework(@PathVariable Integer id) {
        return homeworkService.deleteHomework(id);
    }
    
    @GetMapping("/page")
    public Page<Homework> listHomeworkByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return homeworkService.listHomeworkByPage(current, size);
    }
    
    @GetMapping("/class/{classId}")
    public List<Homework> listHomeworkByClassId(@PathVariable Integer classId) {
        return homeworkService.listHomeworkByClassId(classId);
    }
}
