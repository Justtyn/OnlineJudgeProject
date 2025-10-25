package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Course;
import com.example.onlinejudge.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "课程管理接口")
@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @ApiOperation("添加课程")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCourse(
            @ApiParam("课程信息") @RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
        
        // 设置创建者ID为管理员ID（1），因为外键约束要求creator_id必须引用admin表的id
        course.setCreatorId(1);
        
        boolean result = courseService.addCourse(course);

        if (result) {
            response.put("success", true);
            response.put("message", "课程添加成功");
            response.put("data", course);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "课程添加失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(
            @ApiParam("课程ID") @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean result = courseService.deleteCourse(id);

        if (result) {
            response.put("success", true);
            response.put("message", "课程删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "课程删除失败，可能课程不存在");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation("更新课程")
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateCourse(
            @ApiParam("课程信息") @RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
        
        // 设置创建者ID为管理员ID（1），因为外键约束要求creator_id必须引用admin表的id
        course.setCreatorId(1);
        
        boolean result = courseService.updateCourse(course);

        if (result) {
            response.put("success", true);
            response.put("message", "课程更新成功");
            response.put("data", course);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "课程更新失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation("分页查询所有课程")
    @GetMapping
    public ResponseEntity<Map<String, Object>> listCourses(
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> response = new HashMap<>();
        Page<Course> coursePage = courseService.listCourses(current, size);

        response.put("success", true);
        response.put("data", coursePage);
        return ResponseEntity.ok(response);
    }

    @ApiOperation("根据ID查询课程")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseById(
            @ApiParam("课程ID") @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Course course = courseService.getCourseById(id);

        if (course != null) {
            response.put("success", true);
            response.put("data", course);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "未找到指定课程");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ApiOperation("获取所有课程列表")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> listAllCourses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Course> courses = courseService.listAllCourses();
            response.put("success", true);
            response.put("message", "获取课程列表成功");
            response.put("data", courses);
            response.put("total", courses.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程列表失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
