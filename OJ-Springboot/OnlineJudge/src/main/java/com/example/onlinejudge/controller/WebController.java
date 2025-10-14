package com.example.onlinejudge.controller;

import cn.hutool.core.util.ObjectUtil;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Admin;
// import com.example.onlinejudge.entity.Teacher; // 教师相关接口由 TeacherController 提供
import com.example.onlinejudge.service.AdminService;
import com.example.onlinejudge.service.MailService;
import com.example.onlinejudge.service.StudentService;
import com.example.onlinejudge.service.TeacherService;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.Resource;

@Api(tags = "Web基础接口")
@RestController
//@RequestMapping("")
public class WebController {

    // 注入AdminService服务，用于管理员相关操作
    @Resource
    private AdminService adminService;

    // 注入StudentService服务，用于学生相关操作
    @Resource
    private StudentService studentService;

    // 注入TeacherService服务，用于教师相关操作
    @Resource
    private TeacherService teacherService;

    @Resource
    private MailService mailService;

    @ApiOperation("默认接口")
    @GetMapping("/")
    public Result hello() {
        // 直接返回成功响应，通常用于测试接口或健康检查
        return Result.success();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login(@ApiParam("账号信息") @RequestBody Account account) {
        Account dbAccount;

        // 根据角色来判断登录身份，分别处理管理员、学生和教师的登录逻辑
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {  // 管理员登录
            dbAccount = adminService.login(account);
        } else if (RoleEnum.STUDENT.name().equals(account.getRole())) {  // 学生登录
            dbAccount = studentService.login(account);
        } else if (RoleEnum.TEACHER.name().equals(account.getRole())) {  // 教师登录
            dbAccount = teacherService.login(account);
        } else {
            // 如果角色错误，则返回错误信息
            return Result.error("400", "角色错误");
        }

        // 登录成功后返回用户信息
        return Result.success(dbAccount);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result register(@ApiParam("账号信息") @RequestBody Account account) {
        // 检查账号或密码是否为空
        if (ObjectUtil.isEmpty(account.getUsername()) || ObjectUtil.isEmpty(account.getPassword())) {
            return Result.error("400", "账号或密码必填");  // 返回错误，提示账号或密码不能为空
        }

        // 判断角色，根据角色分别进行注册操作
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            // 如果是管理员角色，调用管理员注册服务
            adminService.register(account);
            // 发送管理员注册通知邮件
            String adminSubject = "新管理员注册通知";
            String adminContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "  body { font-family: Arial, sans-serif; background-color: #f4f7fa; color: #333; padding: 20px; }" +
                    "  h1 { color: #2196F3; }" +
                    "  .message { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                    "  p { font-size: 16px; line-height: 1.6; }" +
                    "  .highlight { color: #2196F3; font-weight: bold; }" +
                    "  .footer { font-size: 14px; color: #777; margin-top: 20px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='message'>" +
                    "<h1>新管理员注册通知</h1>" +
                    "<p>系统通知：有一位新的管理员已成功注册。</p>" +
                    "<p>注册账号：<span class='highlight'>" + account.getUsername() + "</span></p>" +
                    "<p>注册时间：" + java.time.LocalDateTime.now() + "</p>" +
                    "<div class='footer'>" +
                    "<p>此邮件由系统自动发送，请勿回复。</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            mailService.sendHtmlMail("1046220903@qq.com", adminSubject, adminContent);
            return Result.success("注册成功");
        } else if (RoleEnum.TEACHER.name().equals(account.getRole())) {  // 如果是教师角色
            // 调用教师注册服务
            teacherService.register(account);
            // 2. 如果有邮箱，就发送欢迎邮件
            if (ObjectUtil.isNotEmpty(account.getEmail())) {
                String subject = "欢迎加入 XUJCOJ 教师团队";
                // HTML 格式的欢迎邮件内容
                String content = "<html>" +
                        "<head>" +
                        "<style>" +
                        "  body { font-family: Arial, sans-serif; background-color: #f4f7fa; color: #333; padding: 20px; }" +
                        "  h1 { color: #FF9800; }" +
                        "  .message { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                        "  p { font-size: 16px; line-height: 1.6; }" +
                        "  a { color: #FF9800; text-decoration: none; }" +
                        "  .footer { font-size: 14px; color: #777; margin-top: 20px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='message'>" +
                        "<h1>亲爱的 " + account.getUsername() + " 老师，欢迎加入 XUJCOJ 教师团队！</h1>" +
                        "<p>恭喜您成功注册成为 XUJCOJ 的教师！我们为您提供了一个强大的平台，让您可以管理学生、创建课程、布置作业和监控学习进度。</p>" +
                        "<p>作为教师，您将可以享受以下服务：</p>" +
                        "<ul>" +
                        "  <li>创建和管理课程，组织教学内容。</li>" +
                        "  <li>布置作业和编程题目，跟踪学生进度。</li>" +
                        "  <li>查看学生提交记录和成绩统计。</li>" +
                        "  <li>发布公告，与学生进行交流。</li>" +
                        "</ul>" +
                        "<p>为了帮助您更好地开始，您可以访问我们的 " +
                        "<a href='https://justtyn.github.io/' target='_blank'>官网</a>" +
                        " 获取更多的教学资源。</p>" +
                        "<p>再次欢迎您加入我们的教师团队！期待您在这里创造优秀的教学成果！</p>" +
                        "<div class='footer'>" +
                        "<p>如果您有任何问题，随时可以联系我们的<a href='https://github.com/Justtyn' target='_blank'>支持团队</a>。</p>" +
                        "<p>祝您教学愉快！</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

                // 发送 HTML 邮件
                mailService.sendHtmlMail(account.getEmail(), subject, content);
            }
            // 发送教师注册通知邮件
            String teacherSubject = "新教师注册通知";
            String teacherContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "  body { font-family: Arial, sans-serif; background-color: #f4f7fa; color: #333; padding: 20px; }" +
                    "  h1 { color: #FF9800; }" +
                    "  .message { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                    "  p { font-size: 16px; line-height: 1.6; }" +
                    "  .highlight { color: #FF9800; font-weight: bold; }" +
                    "  .footer { font-size: 14px; color: #777; margin-top: 20px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='message'>" +
                    "<h1>新教师注册通知</h1>" +
                    "<p>系统通知：有一位新的教师已成功注册。</p>" +
                    "<p>注册账号：<span class='highlight'>" + account.getUsername() + "</span></p>" +
                    "<p>注册时间：" + java.time.LocalDateTime.now() + "</p>" +
                    "<div class='footer'>" +
                    "<p>此邮件由系统自动发送，请勿回复。</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            mailService.sendHtmlMail("1046220903@qq.com", teacherSubject, teacherContent);
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
            // 发送学生注册通知邮件
            String studentSubject = "新学生注册通知";
            String studentContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "  body { font-family: Arial, sans-serif; background-color: #f4f7fa; color: #333; padding: 20px; }" +
                    "  h1 { color: #4CAF50; }" +
                    "  .message { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                    "  p { font-size: 16px; line-height: 1.6; }" +
                    "  .highlight { color: #4CAF50; font-weight: bold; }" +
                    "  .footer { font-size: 14px; color: #777; margin-top: 20px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='message'>" +
                    "<h1>新学生注册通知</h1>" +
                    "<p>系统通知：有一位新的学生已成功注册。</p>" +
                    "<p>注册账号：<span class='highlight'>" + account.getUsername() + "</span></p>" +
                    "<p>注册时间：" + java.time.LocalDateTime.now() + "</p>" +
                    "<div class='footer'>" +
                    "<p>此邮件由系统自动发送，请勿回复。</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            mailService.sendHtmlMail("1046220903@qq.com", studentSubject, studentContent);
            return Result.success("注册成功");
        }

        // 如果角色不符合要求，返回注册错误
        return Result.error("400", "注册错误");
    }

    @ApiOperation("根据ID查询管理员信息")
    @GetMapping("/admin/{id}")
    public Result getAdminById(@ApiParam("管理员ID") @PathVariable Integer id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }

    // 教师接口已在 TeacherController 暴露，避免与其冲突
}
