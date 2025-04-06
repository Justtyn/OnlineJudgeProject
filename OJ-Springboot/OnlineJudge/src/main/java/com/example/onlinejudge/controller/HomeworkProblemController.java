package com.example.onlinejudge.controller;

import com.example.onlinejudge.entity.HomeworkProblem;
import com.example.onlinejudge.service.HomeworkProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/homework-problem")
public class HomeworkProblemController {
    
    @Autowired
    private HomeworkProblemService homeworkProblemService;
    
    /**
     * 添加作业题目关联
     */
    @PostMapping("/add")
    public ResponseEntity<Boolean> addHomeworkProblem(@RequestBody HomeworkProblem homeworkProblem) {
        boolean result = homeworkProblemService.addHomeworkProblem(homeworkProblem);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除作业题目关联
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Boolean> removeHomeworkProblem(
            @RequestParam("homeworkId") int homeworkId,
            @RequestParam("problemId") int problemId) {
        boolean result = homeworkProblemService.removeHomeworkProblem(homeworkId, problemId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据作业ID查询所有关联的题目
     */
    @GetMapping("/list/{homeworkId}")
    public ResponseEntity<List<HomeworkProblem>> getHomeworkProblemsByHomeworkId(
            @PathVariable("homeworkId") int homeworkId) {
        List<HomeworkProblem> homeworkProblems = homeworkProblemService.getHomeworkProblemsByHomeworkId(homeworkId);
        return ResponseEntity.ok(homeworkProblems);
    }
}
