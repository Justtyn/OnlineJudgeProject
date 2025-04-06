package com.example.onlinejudge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Course;
import com.example.onlinejudge.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 添加课程
     * @param course 课程信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCourse(@RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
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
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Integer id) {
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
    
    /**
     * 分页查询所有课程
     * @param current 当前页，默认为1
     * @param size 每页大小，默认为10
     * @return 课程分页结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listCourses(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> response = new HashMap<>();
        Page<Course> coursePage = courseService.listCourses(current, size);
        
        response.put("success", true);
        response.put("data", coursePage);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable Integer id) {
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
}
