package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Homework;
import com.example.onlinejudge.service.HomeworkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "作业管理接口")
@RestController
@RequestMapping("/homework")
public class HomeworkController {
    
    @Autowired
    private HomeworkService homeworkService;
    
    @ApiOperation("添加作业")
    @PostMapping
    public Result<Boolean> addHomework(@ApiParam("作业信息") @RequestBody Homework homework) {
        boolean success = homeworkService.addHomework(homework);
        return success ? Result.success(true) : Result.error("500", "添加作业失败");
    }
    
    @ApiOperation("删除作业")
    @DeleteMapping("/{id}")
    public boolean deleteHomework(@ApiParam("作业ID") @PathVariable Integer id) {
        return homeworkService.deleteHomework(id);
    }
    
    @ApiOperation("分页查询作业")
    @GetMapping("/page")
    public Result<Page<Homework>> listHomeworkByPage(
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("搜索标题") @RequestParam(required = false) String title,
            @ApiParam("班级ID") @RequestParam(required = false) Integer classId,
            @ApiParam("状态筛选") @RequestParam(required = false) String status) {
        Page<Homework> page = homeworkService.listHomeworkByPage(current, size, title, classId, status);
        return Result.success(page);
    }
    
    @ApiOperation("查询班级作业列表")
    @GetMapping("/class/{classId}")
    public List<Homework> listHomeworkByClassId(@ApiParam("班级ID") @PathVariable Integer classId) {
        return homeworkService.listHomeworkByClassId(classId);
    }
    
    @ApiOperation("更新作业")
    @PutMapping
    public Result<Boolean> updateHomework(@ApiParam("作业信息") @RequestBody Homework homework) {
        boolean success = homeworkService.updateHomework(homework);
        return success ? Result.success(true) : Result.error("500", "更新作业失败");
    }
}
