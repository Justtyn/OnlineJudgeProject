package com.example.onlinejudge.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.service.AdminService;
import com.example.onlinejudge.service.StudentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
//@RequestMapping("")
public class WebController {
    @Resource
    private AdminService adminService;
    @Resource
    private StudentService studentService;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }


    /**
     * 登陆接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account dbAccount;
        // 通过枚举类判断登陆身份 以实现查询单接口不同身份登陆查询
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {  // 管理员登陆
            dbAccount = adminService.login(account);
        } else if (RoleEnum.STUDENT.name().equals(account.getRole())) {  // 学生登陆
            dbAccount = studentService.login(account);
        } else {
            return Result.error("角色错误");
        }
        return Result.success(dbAccount);
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (ObjectUtil.isEmpty(account.getUsername()) || ObjectUtil.isEmpty(account.getPassword())) {
            return Result.error("账号或密码必填");
        }
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            adminService.register(account);
            return Result.success("注册成功");
        } else if (RoleEnum.STUDENT.name().equals(account.getRole())) {  // 学生登陆
            studentService.register(account);
            return Result.success("注册成功");
        }
        return Result.error("注册错误");
    }
}
