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

    // 注入AdminService服务，用于管理员相关操作
    @Resource
    private AdminService adminService;
    
    // 注入StudentService服务，用于学生相关操作
    @Resource
    private StudentService studentService;

    /**
     * 默认请求接口，响应基本的成功状态
     * @return Result.success() 返回一个成功的响应结果
     */
    @GetMapping("/")
    public Result hello() {
        // 直接返回成功响应，通常用于测试接口或健康检查
        return Result.success();
    }

    /**
     * 登录接口，根据传入的账号信息进行角色验证，进行登录操作
     * @param account 用户的账号信息
     * @return 登录成功返回用户信息，失败则返回错误信息
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account dbAccount;
        
        // 根据角色来判断登录身份，分别处理管理员和学生的登录逻辑
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {  // 管理员登录
            dbAccount = adminService.login(account);
        } else if (RoleEnum.STUDENT.name().equals(account.getRole())) {  // 学生登录
            dbAccount = studentService.login(account);
        } else {
            // 如果角色错误，则返回错误信息
            return Result.error("400", "角色错误");
        }
        
        // 登录成功后返回用户信息
        return Result.success(dbAccount);
    }

    /**
     * 注册接口，处理用户的注册请求
     * @param account 用户的账号信息
     * @return 根据不同角色返回注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        // 检查账号或密码是否为空
        if (ObjectUtil.isEmpty(account.getUsername()) || ObjectUtil.isEmpty(account.getPassword())) {
            return Result.error("400", "账号或密码必填");  // 返回错误，提示账号或密码不能为空
        }
        
        // 判断角色，根据角色分别进行注册操作
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            // 如果是管理员角色，调用管理员注册服务
            adminService.register(account);
            return Result.success("注册成功");
        } else if (RoleEnum.STUDENT.name().equals(account.getRole())) {  // 如果是学生角色
            // 调用学生注册服务
            studentService.register(account);
            return Result.success("注册成功");
        }
        
        // 如果角色不符合要求，返回注册错误
        return Result.error("400", "注册错误");
    }
}
