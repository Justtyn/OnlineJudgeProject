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
import java.util.concurrent.ThreadLocalRandom;

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
        Teacher dbTeacher = teacherMapper.selectByUsername(account.getUsername());
        if (dbTeacher == null) {
            throw new CustomException("账号或密码错误");
        }
        if (!securePass(account.getPassword()).equals(dbTeacher.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        String token = JwtUtil.generateToken(dbTeacher.getUsername());

        // 转换为Account对象返回
        Account result = new Account();
        result.setId(dbTeacher.getId()); // 添加ID字段
        result.setUsername(dbTeacher.getUsername());
        result.setRole(dbTeacher.getRole());
        result.setToken(token);
        result.setName(dbTeacher.getName());
        result.setSex(dbTeacher.getSex());
        result.setEmail(dbTeacher.getEmail());
        result.setPhone(dbTeacher.getPhone());
        result.setAvatar(dbTeacher.getAvatar());

        return result;
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
        if (ObjectUtil.isEmpty(teacher.getName())) {
            teacher.setName(teacher.getUsername());
        }
        if (!ObjectUtil.isEmpty(teacher.getPassword())) {
            teacher.setPassword(securePass(teacher.getPassword()));
        }
        teacher.setRole(RoleEnum.TEACHER.name());
        return teacherMapper.update(teacher) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        Teacher dbTeacher = teacherMapper.selectById(id);
        if (dbTeacher == null) {
            throw new CustomException("教师不存在");
        }
        return teacherMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getTeachersWithPage(Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        int offset = (pageNum - 1) * pageSize;
        List<Teacher> teachers = teacherMapper.selectWithPage(offset, pageSize);
        long total = teacherMapper.countAll();

        result.put("list", teachers);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public List<Teacher> getTeachersByUsernameLike(String username) {
        return teacherMapper.selectByUsernameLike(username);
    }

    @Override
    public List<Teacher> getTeachersByNameLike(String name) {
        return teacherMapper.selectByNameLike(name);
    }

    @Override
    public List<Teacher> getTeachersByCreateTimeYear(Integer year) {
        return teacherMapper.selectByCreateTimeYear(year);
    }

    @Override
    public List<Teacher> getTeachersByClassId(Integer classId) {
        return teacherMapper.selectByClassId(classId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        Teacher teacher = teacherMapper.selectByUsername(username);
        return teacher != null;
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("文件不能为空");
        }
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new CustomException("教师不存在");
        }
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new CustomException("创建上传目录失败");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "avatar";
        }
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        String avatarUrl = baseUrl + "/uploads/" + fileName;
        teacher.setAvatar(avatarUrl);
        if (teacherMapper.update(teacher) <= 0) {
            throw new CustomException("更新头像失败");
        }
        return avatarUrl;
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String email) {
        Teacher teacher = teacherMapper.selectByUsername(username);
        if (teacher == null) {
            throw new CustomException("用户名不存在");
        }
        if (teacher.getEmail() == null || !teacher.getEmail().equals(email)) {
            throw new CustomException("邮箱与用户名不匹配");
        }
        String newPassword = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        teacher.setPassword(securePass(newPassword));
        if (teacherMapper.update(teacher) <= 0) {
            throw new CustomException("密码重置失败");
        }

        String subject = "【在线评测系统】教师密码重置通知";
        String htmlContent = String.format(
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>"
                        + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>"
                        + "<h2 style='color: #333; text-align: center;'>密码重置通知</h2>"
                        + "<p style='color: #666;'>尊敬的 <strong>%s</strong> 教师：</p>"
                        + "<p style='color: #666;'>您好！我们已经为您的账号生成了新的临时密码，请使用该密码登录并尽快修改。</p>"
                        + "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>"
                        + "<p style='color: #333; margin: 0;'><strong>新的临时密码：</strong></p>"
                        + "<p style='color: #007bff; font-size: 24px; font-weight: bold; text-align: center; margin: 10px 0;'>%s</p>"
                        + "</div>"
                        + "<p style='color: #666;'>为了您的账号安全，请登录后立即修改密码，如不是本人操作请及时联系管理员。</p>"
                        + "<p style='color: #666; text-align: right;'>在线评测系统团队</p>"
                        + "</div>"
                        + "</div>",
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
        teacher.setPassword(securePass(newPassword));
        if (teacherMapper.update(teacher) <= 0) {
            throw new CustomException("密码修改失败");
        }

        String subject = "【在线评测系统】教师密码修改通知";
        String htmlContent = String.format(
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>"
                        + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>"
                        + "<h2 style='color: #333; text-align: center;'>密码修改通知</h2>"
                        + "<p style='color: #666;'>尊敬的 <strong>%s</strong> 教师：</p>"
                        + "<p style='color: #666;'>您的密码已成功修改，如非本人操作请立即联系管理员。</p>"
                        + "<div style='background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0;'>"
                        + "<p style='color: #333; margin: 0;'><strong>当前密码：</strong></p>"
                        + "<p style='color: #007bff; font-size: 24px; font-weight: bold; text-align: center; margin: 10px 0;'>%s</p>"
                        + "</div>"
                        + "<p style='color: #666;'>请妥善保管您的账号信息，避免泄露。</p>"
                        + "<p style='color: #666; text-align: right;'>在线评测系统团队</p>"
                        + "</div>"
                        + "</div>",
                username, newPassword
        );
        mailService.sendHtmlMail(teacher.getEmail(), subject, htmlContent);
        return true;
    }

    @Override
    public long count() {
        return teacherMapper.countAll();
    }

    @Override
    public long getNewTeacherCount(LocalDate startDate, LocalDate endDate) {
        return teacherMapper.countNewTeachers(startDate, endDate);
    }
}
