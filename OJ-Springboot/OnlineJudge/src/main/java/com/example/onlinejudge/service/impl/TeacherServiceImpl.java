package com.example.onlinejudge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Teacher;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.TeacherMapper;
import com.example.onlinejudge.service.MailService;
import com.example.onlinejudge.service.TeacherService;
import com.example.onlinejudge.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherServiceImpl implements TeacherService {
    private static final String PASS_SALT = "OnlineJudge";

    @Value("${app.base-url}")
    private String baseUrl;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private MailService mailService;

    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    @Override
    @Transactional
    public Account login(Account account) {
        Account dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (dbTeacher == null) {
            throw new CustomException("账号或密码错误");
        }
        if (!securePass(account.getPassword()).equals(dbTeacher.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        String token = JwtUtil.generateToken(dbTeacher.getUsername());
        dbTeacher.setToken(token);
        return dbTeacher;
    }

    @Override
    @Transactional
    public void register(Account account) {
        Teacher teacher = new Teacher();
        teacher.setUsername(account.getUsername());
        teacher.setPassword(account.getPassword());
        teacher.setSex(account.getSex());
        teacher.setEmail(account.getEmail());
        teacher.setPhone(account.getPhone());
        teacher.setClassId(1); // 默认班级ID
        this.add(teacher);
    }

    @Override
    public void add(Teacher teacher) {
        Teacher dbTeacher = teacherMapper.selectByUsername(teacher.getUsername());
        if (dbTeacher != null) {
            throw new CustomException("账号已存在");
        }
        if (ObjectUtil.isEmpty(teacher.getName())) {
            teacher.setName(teacher.getUsername());
        }
        teacher.setPassword(securePass(teacher.getPassword()));
        teacher.setRole(RoleEnum.TEACHER.name());
        teacherMapper.insert(teacher);
    }

    @Override
    public Teacher getTeacherById(Integer id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new CustomException("教师不存在");
        }
        return teacher;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean update(Teacher teacher) {
        Teacher dbTeacher = teacherMapper.selectById(teacher.getId());
        if (dbTeacher == null) {
            throw new CustomException("教师不存在");
        }
        if (!dbTeacher.getUsername().equals(teacher.getUsername())) {
            Teacher existTeacher = teacherMapper.selectByUsername(teacher.getUsername());
            if (existTeacher != null) {
                throw new CustomException("用户名已存在");
            }
        }
        return teacherMapper.update(teacher) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new CustomException("教师不存在");
        }
        return teacherMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new CustomException("教师不存在");
        }
        // 1) 保存文件到磁盘
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        // 2) 用配置的 baseUrl 拼成完整外网地址
        String avatarUrl = baseUrl + "/uploads/" + fileName;

        // 3) 更新数据库并返回
        teacher.setAvatar(avatarUrl);
        int updated = teacherMapper.update(teacher);
        if (updated > 0) {
            return avatarUrl;
        } else {
            throw new CustomException("更新头像失败");
        }
    }

    @Override
    public Map<String, Object> getTeachersWithPage(Integer pageNum, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 获取总记录数
        Integer total = teacherMapper.countAll();

        // 获取分页数据
        List<Teacher> teachers = teacherMapper.selectWithPage(offset, pageSize);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (total + pageSize - 1) / pageSize);
        result.put("current", pageNum);
        result.put("size", pageSize);
        result.put("records", teachers);

        return result;
    }

    @Override
    public List<Teacher> getTeachersByUsernameLike(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new CustomException("用户名关键字不能为空");
        }
        return teacherMapper.selectByUsernameLike(username);
    }

    @Override
    public List<Teacher> getTeachersByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException("姓名关键字不能为空");
        }
        return teacherMapper.selectByNameLike(name);
    }

    @Override
    public List<Teacher> getTeachersByCreateTimeYear(Integer year) {
        if (year == null || year < 1900 || year > 2100) {
            throw new CustomException("年份参数无效");
        }
        return teacherMapper.selectByCreateTimeYear(year);
    }

    @Override
    public List<Teacher> getTeachersByClassId(Integer classId) {
        if (classId == null) {
            throw new CustomException("班级ID不能为空");
        }
        return teacherMapper.selectByClassId(classId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return teacherMapper.selectByUsername(username) != null;
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String email) {
        Teacher teacher = teacherMapper.selectByUsername(username);
        if (teacher == null) {
            throw new CustomException("用户名不存在");
        }
        if (!teacher.getEmail().equals(email)) {
            throw new CustomException("邮箱与用户名不匹配");
        }

        // 生成6位随机密码
        String newPassword = String.format("%06d", (int) (Math.random() * 1000000));
        // 加密新密码
        String encryptedPassword = securePass(newPassword);

        // 更新数据库中的密码
        teacher.setPassword(encryptedPassword);
        teacherMapper.update(teacher);

        // 发送邮件
        String subject = "【在线评测系统】密码重置通知";
        String htmlContent = String.format(
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
                        "<h2 style='color: #333; text-align: center;'>密码重置通知</h2>" +
                        "<p style='color: #666;'>亲爱的 <strong>%s</strong> 老师：</p>" +
                        "<p style='color: #666;'>您好！</p>" +
                        "<p style='color: #666;'>我们收到了您的密码重置请求。为了保障您的账号安全，系统已为您生成新的临时密码。</p>" +
                        "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #333; margin: 0;'><strong>您的新密码是：</strong></p>" +
                        "<p style='color: #007bff; font-size: 24px; font-weight: bold; text-align: center; margin: 10px 0;'>%s</p>" +
                        "</div>" +
                        "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #333; margin: 0;'><strong>请注意：</strong></p>" +
                        "<ul style='color: #666;'>" +
                        "<li>请使用此临时密码尽快登录系统</li>" +
                        "<li>登录后请立即修改密码</li>" +
                        "<li>请勿将此密码告知他人</li>" +
                        "</ul>" +
                        "</div>" +
                        "<p style='color: #666;'>如果这不是您本人的操作，请忽略此邮件。</p>" +
                        "<p style='color: #666;'>祝您使用愉快！</p>" +
                        "<p style='color: #666; text-align: right;'>在线评测系统团队</p>" +
                        "</div>" +
                        "</div>",
                username, newPassword
        );
        mailService.sendHtmlMail(email, subject, htmlContent);

        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Teacher teacher = teacherMapper.selectByUsername(username);
        if (teacher == null) {
            throw new CustomException("用户名不存在");
        }
        if (!securePass(oldPassword).equals(teacher.getPassword())) {
            throw new CustomException("旧密码错误");
        }

        // 加密新密码
        String encryptedPassword = securePass(newPassword);

        // 更新数据库中的密码
        teacher.setPassword(encryptedPassword);
        teacherMapper.update(teacher);

        // 发送邮件
        String subject = "【在线评测系统】密码修改通知";
        String htmlContent = String.format(
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
                        "<h2 style='color: #333; text-align: center;'>密码修改通知</h2>" +
                        "<p style='color: #666;'>亲爱的 <strong>%s</strong> 老师：</p>" +
                        "<p style='color: #666;'>您好！</p>" +
                        "<p style='color: #666;'>您的密码已经成功修改。</p>" +
                        "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #333; margin: 0;'><strong>您的新密码是：</strong></p>" +
                        "<p style='color: #007bff; font-size: 24px; font-weight: bold; text-align: center; margin: 10px 0;'>%s</p>" +
                        "</div>" +
                        "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #333; margin: 0;'><strong>请注意：</strong></p>" +
                        "<ul style='color: #666;'>" +
                        "<li>请妥善保管您的新密码</li>" +
                        "<li>请勿将此密码告知他人</li>" +
                        "<li>如果这不是您本人的操作，请立即联系管理员</li>" +
                        "</ul>" +
                        "</div>" +
                        "<p style='color: #666;'>祝您使用愉快！</p>" +
                        "<p style='color: #666; text-align: right;'>在线评测系统团队</p>" +
                        "</div>" +
                        "</div>",
                username, newPassword
        );
        mailService.sendHtmlMail(teacher.getEmail(), subject, htmlContent);

        return true;
    }

    @Override
    public long count() {
        return teacherMapper.selectCount(null);
    }

    @Override
    public long getNewTeacherCount(LocalDate startDate, LocalDate endDate) {
        return teacherMapper.countNewTeachers(startDate, endDate);
    }
}
