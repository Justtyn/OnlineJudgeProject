package com.example.onlinejudge.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Teacher;
import com.example.onlinejudge.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(tags = "教师管理接口")
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @ApiOperation("添加教师")
    @PostMapping("/add")
    public Result add(@ApiParam("教师信息") @RequestBody Teacher teacher) {
        try {
            teacherService.add(teacher);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("根据ID查询教师")
    @GetMapping("/{id}")
    public Result getTeacherById(@ApiParam("教师ID") @PathVariable Integer id) {
        try {
            Teacher teacher = teacherService.getTeacherById(id);
            return Result.success(teacher);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("查询所有教师")
    @GetMapping("/all")
    public Result getAllTeachers() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("分页查询教师")
    @GetMapping("/page")
    public Result getTeachersWithPage(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                     @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = teacherService.getTeachersWithPage(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("更新教师信息")
    @PutMapping("/update")
    public Result update(@ApiParam("教师信息") @RequestBody Teacher teacher) {
        try {
            boolean success = teacherService.update(teacher);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("500", "更新失败");
            }
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("删除教师")
    @DeleteMapping("/{id}")
    public Result delete(@ApiParam("教师ID") @PathVariable Integer id) {
        try {
            boolean success = teacherService.delete(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("500", "删除失败");
            }
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("上传教师头像")
    @PostMapping("/upload/avatar/{id}")
    public Result uploadAvatar(@ApiParam("头像文件") @RequestParam("file") MultipartFile file,
                              @ApiParam("教师ID") @PathVariable Integer id) {
        try {
            if (file.isEmpty()) {
                return Result.error("400", "文件不能为空");
            }
            String avatarUrl = teacherService.uploadAvatar(file, id);
            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("500", "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("根据用户名模糊查询教师")
    @GetMapping("/search/username")
    public Result getTeachersByUsernameLike(@ApiParam("用户名关键字") @RequestParam String username) {
        try {
            if (ObjectUtil.isEmpty(username)) {
                return Result.error("400", "用户名关键字不能为空");
            }
            List<Teacher> teachers = teacherService.getTeachersByUsernameLike(username);
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("根据姓名模糊查询教师")
    @GetMapping("/search/name")
    public Result getTeachersByNameLike(@ApiParam("姓名关键字") @RequestParam String name) {
        try {
            if (ObjectUtil.isEmpty(name)) {
                return Result.error("400", "姓名关键字不能为空");
            }
            List<Teacher> teachers = teacherService.getTeachersByNameLike(name);
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("根据创建年份查询教师")
    @GetMapping("/search/year")
    public Result getTeachersByCreateTimeYear(@ApiParam("年份") @RequestParam Integer year) {
        try {
            if (year == null || year < 1900 || year > 2100) {
                return Result.error("400", "年份参数无效");
            }
            List<Teacher> teachers = teacherService.getTeachersByCreateTimeYear(year);
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("根据班级ID查询教师")
    @GetMapping("/search/class/{classId}")
    public Result getTeachersByClassId(@ApiParam("班级ID") @PathVariable Integer classId) {
        try {
            if (classId == null) {
                return Result.error("400", "班级ID不能为空");
            }
            List<Teacher> teachers = teacherService.getTeachersByClassId(classId);
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("检查用户名是否存在")
    @GetMapping("/check/username")
    public Result isUsernameExists(@ApiParam("用户名") @RequestParam String username) {
        try {
            if (ObjectUtil.isEmpty(username)) {
                return Result.error("400", "用户名不能为空");
            }
            boolean exists = teacherService.isUsernameExists(username);
            return Result.success(exists);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("找回密码")
    @PostMapping("/reset-password")
    public Result resetPassword(@ApiParam("用户名") @RequestParam String username,
                               @ApiParam("邮箱") @RequestParam String email) {
        try {
            if (ObjectUtil.isEmpty(username) || ObjectUtil.isEmpty(email)) {
                return Result.error("400", "用户名和邮箱不能为空");
            }
            boolean success = teacherService.resetPassword(username, email);
            if (success) {
                return Result.success("密码重置邮件已发送");
            } else {
                return Result.error("500", "密码重置失败");
            }
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("修改密码")
    @PostMapping("/change-password")
    public Result changePassword(@ApiParam("用户名") @RequestParam String username,
                                @ApiParam("旧密码") @RequestParam String oldPassword,
                                @ApiParam("新密码") @RequestParam String newPassword) {
        try {
            if (ObjectUtil.isEmpty(username) || ObjectUtil.isEmpty(oldPassword) || ObjectUtil.isEmpty(newPassword)) {
                return Result.error("400", "用户名、旧密码和新密码不能为空");
            }
            boolean success = teacherService.changePassword(username, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("500", "密码修改失败");
            }
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    @ApiOperation("获取教师总数")
    @GetMapping("/count")
    public Result getTeacherCount() {
        try {
            long count = teacherService.count();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }
}
