package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.service.StudentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    @Resource
    StudentService studentService;

    // 新增学生
    @PostMapping("/add")
    public Result add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }
    
    // 查询学生信息（根据id）
    @GetMapping("/{id}")
    public Result getStudent(@PathVariable("id") Integer id) {
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }
    
    // 修改学生信息
    @PutMapping("/update")
    public Result update(@RequestBody Student student) {
        boolean updated = studentService.update(student);
        return updated ? Result.success() : Result.error("更新失败");
    }
    
    // 删除学生
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        boolean deleted = studentService.delete(id);
        return deleted ? Result.success() : Result.error("删除失败");
    }
    
    // 查询所有学生信息
    @GetMapping("/all")
    public Result getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return Result.success(students);
    }
}
