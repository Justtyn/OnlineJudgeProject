package com.example.onlinejudge.controller;

import cn.hutool.core.util.ObjectUtil;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Admin;
import com.example.onlinejudge.service.AdminService;
import com.example.onlinejudge.service.MailService;
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


    @Resource
    private MailService mailService;

    /**
     * 默认请求接口，响应基本的成功状态
     *
     * @return Result.success() 返回一个成功的响应结果
     */
    @GetMapping("/")
    public Result hello() {
        // 直接返回成功响应，通常用于测试接口或健康检查
        return Result.success();
    }

    /**
     * 登录接口，根据传入的账号信息进行角色验证，进行登录操作
     *
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
     *
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
            // 2. 如果有邮箱，就发送欢迎邮件
            if (ObjectUtil.isNotEmpty(account.getEmail())) {
                String subject = "欢迎加入 XUJCOJ";
                // HTML 格式的欢迎邮件内容
                String content = "<html>" +
                        "<head>" +
                        "<style>" +
                        "  body { font-family: Arial, sans-serif; background-color: #f4f7fa; color: #333; padding: 20px; }" +
                        "  h1 { color: #4CAF50; }" +
                        "  .message { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                        "  p { font-size: 16px; line-height: 1.6; }" +
                        "  a { color: #4CAF50; text-decoration: none; }" +
                        "  .footer { font-size: 14px; color: #777; margin-top: 20px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='message'>" +
                        "<h1>亲爱的 " + account.getUsername() + "，欢迎加入 XUJCOJ！</h1>" +
                        "<p>恭喜您成功注册成为 XUJCOJ 的一员！我们为您提供了一个平台，让您可以通过解答各种有趣且挑战性的编程题目来提升技能。</p>" +
                        "<p>作为注册用户，您将可以享受以下服务：</p>" +
                        "<ul>" +
                        "  <li>访问丰富的编程题库，涵盖各类算法与数据结构问题。</li>" +
                        "  <li>参与定期的编程竞赛，展示您的编程水平。</li>" +
                        "  <li>查看详细的解题报告，帮助您不断进步。</li>" +
                        "</ul>" +
                        "<p>为了帮助您更好地开始，您可以访问我们的 " +
                        "<a href='https://justtyn.github.io/' target='_blank'>官网</a>" +
                        " 获取更多的学习资源。</p>" +
                        "<p>再次欢迎您加入我们！期待您在这里取得优异的成绩！</p>" +
                        "<div class='footer'>" +
                        "<p>如果您有任何问题，随时可以联系我们的<a href='https://github.com/Justtyn' target='_blank'>支持团队</a>。</p>" +
                        "<p>祝您学习愉快！</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                // 发送 HTML 邮件
                mailService.sendHtmlMail(account.getEmail(), subject, content);
            }
            return Result.success("注册成功");
        }

        // 如果角色不符合要求，返回注册错误
        return Result.error("400", "注册错误");
    }

    /**
     * 根据ID查询管理员信息
     *
     * @param id 管理员ID
     * @return 返回管理员信息
     */
    @GetMapping("/admin/{id}")
    public Result getAdminById(@PathVariable Integer id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }
}
