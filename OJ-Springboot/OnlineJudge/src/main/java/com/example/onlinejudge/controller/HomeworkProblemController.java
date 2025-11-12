package com.example.onlinejudge.controller;

import com.example.onlinejudge.entity.HomeworkProblem;
import com.example.onlinejudge.service.HomeworkProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

@Api(tags = "作业题目关联管理接口")
@RestController
@RequestMapping("/homework-problem")
public class HomeworkProblemController {

    @Autowired
    private HomeworkProblemService homeworkProblemService;

    @ApiOperation("添加作业题目关联")
    @PostMapping("/add")
    public ResponseEntity<Boolean> addHomeworkProblem(
            @ApiParam("作业题目关联信息") @RequestBody HomeworkProblem homeworkProblem) {
        boolean result = homeworkProblemService.addHomeworkProblem(homeworkProblem);
        return ResponseEntity.ok(result);
    }

    @ApiOperation("删除作业题目关联")
    @DeleteMapping("/remove")
    public ResponseEntity<Boolean> removeHomeworkProblem(
            @ApiParam("作业ID") @RequestParam("homeworkId") int homeworkId,
            @ApiParam("题目ID") @RequestParam("problemId") int problemId) {
        boolean result = homeworkProblemService.removeHomeworkProblem(homeworkId, problemId);
        return ResponseEntity.ok(result);
    }

    @ApiOperation("查询作业关联的题目列表")
    @GetMapping("/list/{homeworkId}")
    public ResponseEntity<List<HomeworkProblem>> getHomeworkProblemsByHomeworkId(
            @ApiParam("作业ID") @PathVariable("homeworkId") int homeworkId) {
        List<HomeworkProblem> homeworkProblems = homeworkProblemService.getHomeworkProblemsByHomeworkId(homeworkId);
        return ResponseEntity.ok(homeworkProblems);
    }

    @ApiOperation("增加题目提交次数")
    @PutMapping("/increment-submit")
    public ResponseEntity<Boolean> incrementSubmitCount(
            @ApiParam("作业ID") @RequestParam("homeworkId") int homeworkId,
            @ApiParam("题目ID") @RequestParam("problemId") int problemId) {
        boolean result = homeworkProblemService.incrementSubmitCount(homeworkId, problemId);
        return ResponseEntity.ok(result);
    }

    @ApiOperation("增加题目通过次数")
    @PutMapping("/increment-ac")
    public ResponseEntity<Boolean> incrementAcCount(
            @ApiParam("作业ID") @RequestParam("homeworkId") int homeworkId,
            @ApiParam("题目ID") @RequestParam("problemId") int problemId) {
        boolean result = homeworkProblemService.incrementAcCount(homeworkId, problemId);
        return ResponseEntity.ok(result);
    }
}
