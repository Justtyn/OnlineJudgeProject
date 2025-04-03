package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.service.StudentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 学生管理控制器
 * 处理与学生相关的HTTP请求，包括增删改查、头像上传和背景更新等功能
 * 使用RESTful API设计风格
 */
@RestController // 标识这是一个REST控制器，返回的数据会自动转换为JSON格式
@RequestMapping("/api/student") // 设置该控制器的基础URL路径
public class StudentController {
    @Resource // 自动注入StudentService依赖
    StudentService studentService;

    /**
     * 新增学生
     * 
     * @param student 学生实体对象，从请求体中获取
     * @return 操作结果
     */
    @PostMapping("/add") // 处理POST请求，路径为/api/student/add
    public Result add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    /**
     * 根据ID查询学生信息
     * 
     * @param id 学生ID，从URL路径中获取
     * @return 包含学生信息的操作结果
     */
    @GetMapping("/{id}") // 处理GET请求，路径为/api/student/{id}
    public Result getStudent(@PathVariable("id") Integer id) {
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }

    /**
     * 修改学生信息
     * 
     * @param student 更新后的学生实体对象，从请求体中获取
     * @return 操作结果，成功或失败
     */
    @PutMapping("/update") // 处理PUT请求，路径为/api/student/update
    public Result update(@RequestBody Student student) {
        boolean updated = studentService.update(student);
        return updated ? Result.success() : Result.error("500", "更新失败");
    }

    /**
     * 删除学生
     * 
     * @param id 要删除的学生ID，从URL路径中获取
     * @return 操作结果，成功或失败
     */
    @DeleteMapping("/delete/{id}") // 处理DELETE请求，路径为/api/student/delete/{id}
    public Result delete(@PathVariable("id") Integer id) {
        boolean deleted = studentService.delete(id);
        return deleted ? Result.success() : Result.error("500", "删除失败");
    }

    /**
     * 查询所有学生信息
     * 
     * @return 包含所有学生列表的操作结果
     */
    @GetMapping("/all") // 处理GET请求，路径为/api/student/all
    public Result getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return Result.success(students);
    }

    /**
     * 上传学生头像
     * 
     * @param file 上传的头像文件
     * @param id 学生ID
     * @return 包含头像URL的操作结果，成功或失败
     */
    @PostMapping("/uploadAvatar") // 处理POST请求，路径为/api/student/uploadAvatar
    public Result uploadAvatar(@RequestParam("file") MultipartFile file,
                               @RequestParam("id") Integer id) {
        try {
            String avatarUrl = studentService.uploadAvatar(file, id);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新学生个人背景图片
     * 
     * @param map 包含学生ID和背景图片URL的Map对象
     * @return 操作结果，成功或失败
     */
    @PostMapping("/updateBackground") // 处理POST请求，路径为/api/student/updateBackground
    public Result updateBackground(@RequestBody Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String backgroundImage = (String) map.get("background");
        // 参数校验
        if (id == null || backgroundImage == null) {
            return Result.error("400", "参数错误");
        }
        // 检查学生是否存在
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("404", "学生不存在");
        }
        // 设置新的背景图片
        student.setBackground(backgroundImage);
        // 更新背景图片
        boolean updated = studentService.updateBackground((Integer) map.get("id"), (String) map.get("background"));
        return updated ? Result.success() : Result.error("500", "背景更新失败");
    }

    /**
     * 增加学生的AC题目数
     * 
     * @param id 学生ID
     * @return 操作结果
     */
    @PutMapping("/incrementAc/{id}")
    public Result incrementAc(@PathVariable("id") Integer id) {
        // 检查学生是否存在
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("404", "学生不存在");
        }
        boolean updated = studentService.incrementAc(id);
        return updated ? Result.success() : Result.error("500", "AC数更新失败");
    }

    /**
     * 增加学生的提交次数
     * 
     * @param id 学生ID
     * @return 操作结果
     */
    @PutMapping("/incrementSubmit/{id}")
    public Result incrementSubmit(@PathVariable("id") Integer id) {
        // 检查学生是否存在
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("404", "学生不存在");
        }
        boolean updated = studentService.incrementSubmit(id);
        return updated ? Result.success() : Result.error("500", "提交次数更新失败");
    }

    /**
     * 根据AC排名返回所有学生信息
     * 
     * @return 返回按AC数量降序排列的学生列表
     */
    @GetMapping("/rankByAc")
    public Result getRankByAc() {
        List<Student> students = studentService.getStudentsOrderByAc();
        return Result.success(students);
    }
}

