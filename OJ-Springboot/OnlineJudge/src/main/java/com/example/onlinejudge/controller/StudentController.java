package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.service.StudentService;
import com.example.onlinejudge.exception.CustomException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 学生管理控制器
 * 处理与学生相关的HTTP请求，包括增删改查、头像上传和背景更新等功能
 * 使用RESTful API设计风格
 */
@Api(tags = "学生管理接口")
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
    @ApiOperation("新增学生")
    @PostMapping("/add") // 处理POST请求，路径为/api/student/add
    public Result add(@ApiParam("学生信息") @RequestBody Student student) {
        // 检查用户名是否已存在
        if (studentService.isUsernameExists(student.getUsername())) {
            return Result.error("400", "用户名已存在");
        }
        studentService.add(student);
        return Result.success();
    }

    /**
     * 根据ID查询学生信息
     *
     * @param id 学生ID，从URL路径中获取
     * @return 包含学生信息的操作结果
     */
    @ApiOperation("根据ID查询学生信息")
    @GetMapping("/{id}") // 处理GET请求，路径为/api/student/{id}
    public Result getStudent(@ApiParam("学生ID") @PathVariable("id") Integer id) {
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }

    /**
     * 修改学生信息
     *
     * @param student 更新后的学生实体对象，从请求体中获取
     * @return 操作结果，成功或失败
     */
    @ApiOperation("修改学生信息")
    @PutMapping("/update") // 处理PUT请求，路径为/api/student/update
    public Result update(@ApiParam("学生信息") @RequestBody Student student) {
        // 获取原有学生信息
        Student existingStudent = studentService.getStudentById(student.getId());
        if (existingStudent == null) {
            return Result.error("404", "学生不存在");
        }
        // 保留原有密码、AC数和提交次数
        student.setPassword(existingStudent.getPassword());
        student.setAc(existingStudent.getAc());
        student.setSubmit(existingStudent.getSubmit());
        boolean updated = studentService.update(student);
        return updated ? Result.success() : Result.error("500", "更新失败");
    }

    /**
     * 删除学生
     *
     * @param id 要删除的学生ID，从URL路径中获取
     * @return 操作结果，成功或失败
     */
    @ApiOperation("删除学生")
    @DeleteMapping("/delete/{id}") // 处理DELETE请求，路径为/api/student/delete/{id}
    public Result delete(@ApiParam("学生ID") @PathVariable("id") Integer id) {
        boolean deleted = studentService.delete(id);
        return deleted ? Result.success() : Result.error("500", "删除失败");
    }

    /**
     * 查询所有学生信息
     *
     * @return 包含所有学生列表的操作结果
     */
    @ApiOperation("查询所有学生")
    @GetMapping("/all") // 处理GET请求，路径为/api/student/all
    public Result getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return Result.success(students);
    }

    /**
     * 上传学生头像
     *
     * @param file 上传的头像文件
     * @param id   学生ID
     * @return 包含头像URL的操作结果，成功或失败
     */
    @ApiOperation("上传学生头像")
    @PostMapping("/uploadAvatar") // 处理POST请求，路径为/api/student/uploadAvatar
    public Result uploadAvatar(@ApiParam("头像文件") @RequestParam("file") MultipartFile file,
                               @ApiParam("学生ID") @RequestParam("id") Integer id) {
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
    @ApiOperation("更新学生背景图片")
    @PostMapping("/updateBackground") // 处理POST请求，路径为/api/student/updateBackground
    public Result updateBackground(@ApiParam("更新信息") @RequestBody Map<String, Object> map) {
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
        boolean updated = studentService.updateBackground(id, backgroundImage);
        return updated ? Result.success() : Result.error("500", "背景更新失败");
    }

    /**
     * 增加学生的AC题目数
     *
     * @param id 学生ID
     * @return 操作结果
     */
    @ApiOperation("增加学生AC题目数")
    @PutMapping("/incrementAc/{id}")
    public Result incrementAc(@ApiParam("学生ID") @PathVariable("id") Integer id) {
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
    @ApiOperation("增加学生提交次数")
    @PutMapping("/incrementSubmit/{id}")
    public Result incrementSubmit(@ApiParam("学生ID") @PathVariable("id") Integer id) {
        // 检查学生是否存在
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("404", "学生不存在");
        }
        boolean updated = studentService.incrementSubmit(id);
        return updated ? Result.success() : Result.error("500", "提交次数更新失败");
    }

    /**
     * 根据AC排名返回所有学生信息（分页）
     *
     * @param pageNum  当前页码
     * @param pageSize 每页显示数量
     * @return 返回分页后的、按AC数量降序排列的学生列表
     */
    @ApiOperation("根据AC排名查询学生")
    @GetMapping("/rankByAc")
    public Result getRankByAc(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // 参数校验
        if (pageNum < 1 || pageSize < 1) {
            return Result.error("400", "页码和每页显示数量必须大于0");
        }

        Map<String, Object> pageResult = studentService.getStudentsOrderByAc(pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 根据用户名模糊查询学生信息
     *
     * @param username 用户名关键字
     * @return 包含匹配学生列表的操作结果
     */
    @ApiOperation("根据用户名模糊查询学生")
    @GetMapping("/searchByUsername")
    public Result searchByUsername(@ApiParam("用户名关键字") @RequestParam String username) {
        try {
            List<Student> students = studentService.getStudentsByUsernameLike(username);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 根据姓名模糊查询学生信息
     *
     * @param name 姓名关键字
     * @return 包含匹配学生列表的操作结果
     */
    @ApiOperation("根据姓名模糊查询学生")
    @GetMapping("/searchByName")
    public Result searchByName(@ApiParam("姓名关键字") @RequestParam String name) {
        try {
            List<Student> students = studentService.getStudentsByNameLike(name);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 根据创建时间的年份查询学生信息
     *
     * @param year 年份，如2023
     * @return 包含匹配学生列表的操作结果
     */
    @ApiOperation("根据创建年份查询学生")
    @GetMapping("/searchByYear")
    public Result searchByYear(@ApiParam("年份") @RequestParam Integer year) {
        try {
            List<Student> students = studentService.getStudentsByCreateTimeYear(year);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 找回密码
     *
     * @param map 包含用户名和邮箱的Map对象
     * @return 操作结果
     */
    @ApiOperation("找回密码")
    @PostMapping("/resetPassword")
    public Result resetPassword(
            @ApiParam(value = "重置信息", example = "{\"username\":\"user123\",\"email\":\"user@example.com\"}")
            @RequestBody Map<String, String> map) {
        String username = map.get("username");
        String email = map.get("email");

        if (username == null || email == null) {
            return Result.error("400", "用户名和邮箱不能为空");
        }

        try {
            boolean success = studentService.resetPassword(username, email);
            return success ? Result.success() : Result.error("500", "密码找回失败");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param params 包含username、oldPassword和newPassword的Map
     * @return 修改结果
     */
    @ApiOperation("修改密码")
    @PostMapping("/changePassword")
    public Result<?> changePassword(
            @ApiParam("密码修改信息") @RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return Result.error("400", "参数不完整");
        }

        try {
            boolean success = studentService.changePassword(username, oldPassword, newPassword);
            return success ? Result.success() : Result.error("500", "修改密码失败");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 更新学生的每日挑战状态
     *
     * @param map 包含学生ID和每日挑战状态的Map对象
     * @return 操作结果
     */
    @ApiOperation("更新学生每日挑战状态")
    @PutMapping("/updateDailyChallenge")
    public Result updateDailyChallenge(
            @ApiParam("更新信息") @RequestBody Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String dailyChallenge = (String) map.get("dailyChallenge");

        if (id == null || dailyChallenge == null) {
            return Result.error("400", "参数错误");
        }

        try {
            boolean updated = studentService.updateDailyChallenge(id, dailyChallenge);
            return updated ? Result.success() : Result.error("500", "更新失败");
        } catch (CustomException e) {
            return Result.error("404", e.getMessage());
        }
    }
}

