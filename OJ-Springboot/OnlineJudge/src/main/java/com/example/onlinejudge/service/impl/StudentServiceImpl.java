package com.example.onlinejudge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.StudentMapper;
import com.example.onlinejudge.service.MailService;
import com.example.onlinejudge.service.StudentService;
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
public class StudentServiceImpl implements StudentService {
    private static final String PASS_SALT = "OnlineJudge";

    @Value("${app.base-url}")
    private String baseUrl;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private MailService mailService;

    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    @Override
    @Transactional
    public Account login(Account account) {
        Account dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (dbStudent == null) {
            throw new CustomException("账号或密码错误");
        }
        if (!securePass(account.getPassword()).equals(dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        String token = JwtUtil.generateToken(dbStudent.getUsername());
        dbStudent.setToken(token);
        return dbStudent;
    }

    @Override
    @Transactional
    public void register(Account account) {
        Student student = new Student();
        student.setUsername(account.getUsername());
        student.setPassword(account.getPassword());
        student.setSex(account.getSex());
        student.setEmail(account.getEmail());
        student.setPhone(account.getPhone());
        student.setClassId(1);
        this.add(student);
    }

    @Override
    public void add(Student student) {
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (dbStudent != null) {
            throw new CustomException("账号已存在");
        }
        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setPassword(securePass(student.getPassword()));
        student.setRole(RoleEnum.STUDENT.name());
        studentMapper.insert(student);
    }

    @Override
    public Student getStudentById(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean update(Student student) {
        Student dbStudent = studentMapper.selectById(student.getId());
        if (dbStudent == null) {
            throw new CustomException("学生不存在");
        }
        if (!dbStudent.getUsername().equals(student.getUsername())) {
            Student existStudent = studentMapper.selectByUsername(student.getUsername());
            if (existStudent != null) {
                throw new CustomException("用户名已存在");
            }
        }
        return studentMapper.update(student) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return studentMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
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
        student.setAvatar(avatarUrl);
        int updated = studentMapper.update(student);
        if (updated > 0) {
            return avatarUrl;
        } else {
            throw new CustomException("更新头像失败");
        }
    }

    @Override
    @Transactional
    public boolean updateBackground(Integer id, String background) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return studentMapper.updateBackground(id, background) > 0;
    }

    @Override
    public boolean incrementAc(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return false;
        }
        student.setAc(student.getAc() + 1);
        return studentMapper.update(student) > 0;
    }

    @Override
    public boolean incrementSubmit(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return false;
        }
        student.setSubmit(student.getSubmit() + 1);
        return studentMapper.update(student) > 0;
    }

    @Override
    public List<Student> getStudentsOrderByAc() {
        return studentMapper.selectAllOrderByAc();
    }

    @Override
    public Map<String, Object> getStudentsOrderByAc(Integer pageNum, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 获取总记录数
        Integer total = studentMapper.countAll();

        // 获取分页数据
        List<Student> students = studentMapper.selectOrderByAcWithPage(offset, pageSize);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (total + pageSize - 1) / pageSize);
        result.put("current", pageNum);
        result.put("size", pageSize);
        result.put("records", students);

        return result;
    }

    @Override
    public List<Student> getStudentsByUsernameLike(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new CustomException("用户名关键字不能为空");
        }
        return studentMapper.selectByUsernameLike(username);
    }

    @Override
    public List<Student> getStudentsByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException("姓名关键字不能为空");
        }
        return studentMapper.selectByNameLike(name);
    }

    @Override
    public List<Student> getStudentsByCreateTimeYear(Integer year) {
        if (year == null || year < 1900 || year > 2100) {
            throw new CustomException("年份参数无效");
        }
        return studentMapper.selectByCreateTimeYear(year);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return studentMapper.selectByUsername(username) != null;
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String email) {
        Student student = studentMapper.selectByUsername(username);
        if (student == null) {
            throw new CustomException("用户名不存在");
        }
        if (!student.getEmail().equals(email)) {
            throw new CustomException("邮箱与用户名不匹配");
        }

        // 生成6位随机密码
<<<<<<< HEAD
        String newPassword = String.format("%06d", (int) (Math.random() * 1000000));
        // 加密新密码
        String encryptedPassword = securePass(newPassword);

=======
        String newPassword = String.format("%06d", (int)(Math.random() * 1000000));
        // 加密新密码
        String encryptedPassword = securePass(newPassword);
        
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
        // 更新数据库中的密码
        student.setPassword(encryptedPassword);
        studentMapper.update(student);

        // 发送邮件
        String subject = "【在线评测系统】密码重置通知";
        String htmlContent = String.format(
<<<<<<< HEAD
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
                        "<h2 style='color: #333; text-align: center;'>密码重置通知</h2>" +
                        "<p style='color: #666;'>亲爱的 <strong>%s</strong> 用户：</p>" +
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

=======
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
            "<h2 style='color: #333; text-align: center;'>密码重置通知</h2>" +
            "<p style='color: #666;'>亲爱的 <strong>%s</strong> 用户：</p>" +
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
        
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Student student = studentMapper.selectByUsername(username);
        if (student == null) {
            throw new CustomException("用户名不存在");
        }
        if (!securePass(oldPassword).equals(student.getPassword())) {
            throw new CustomException("旧密码错误");
        }

        // 加密新密码
        String encryptedPassword = securePass(newPassword);
<<<<<<< HEAD

=======
        
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
        // 更新数据库中的密码
        student.setPassword(encryptedPassword);
        studentMapper.update(student);

        // 发送邮件
        String subject = "【在线评测系统】密码修改通知";
        String htmlContent = String.format(
<<<<<<< HEAD
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
                        "<h2 style='color: #333; text-align: center;'>密码修改通知</h2>" +
                        "<p style='color: #666;'>亲爱的 <strong>%s</strong> 用户：</p>" +
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
        mailService.sendHtmlMail(student.getEmail(), subject, htmlContent);

        return true;
    }

    @Override
    @Transactional
    public boolean updateDailyChallenge(Integer id, String dailyChallenge) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return studentMapper.updateDailyChallenge(id, dailyChallenge) > 0;
    }

    @Override
    @Transactional
    public boolean resetAllDailyChallenge() {
        return studentMapper.resetAllDailyChallenge() > 0;
    }

    @Override
    public long count() {
        return studentMapper.selectCount(null);
    }

    @Override
    public long getActiveStudentsCount() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return studentMapper.countActiveStudents(thirtyDaysAgo);
    }

    @Override
    public Map<String, Long> getAcCountDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("0", studentMapper.countByAcRange(0, 0));
        distribution.put("1-10", studentMapper.countByAcRange(1, 10));
        distribution.put("11-50", studentMapper.countByAcRange(11, 50));
        distribution.put("51-100", studentMapper.countByAcRange(51, 100));
        distribution.put("100+", studentMapper.countByAcRange(101, Integer.MAX_VALUE));
        return distribution;
    }

    @Override
    public Map<String, Long> getSubmitCountDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("0", studentMapper.countBySubmitRange(0, 0));
        distribution.put("1-10", studentMapper.countBySubmitRange(1, 10));
        distribution.put("11-50", studentMapper.countBySubmitRange(11, 50));
        distribution.put("51-100", studentMapper.countBySubmitRange(51, 100));
        distribution.put("100+", studentMapper.countBySubmitRange(101, Integer.MAX_VALUE));
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getTopPerformers(int limit) {
        return studentMapper.selectTopPerformers(limit);
    }

    @Override
    public long getNewUserCount(LocalDate startDate, LocalDate endDate) {
        return studentMapper.countNewUsers(startDate, endDate);
    }
=======
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;'>" +
            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>" +
            "<h2 style='color: #333; text-align: center;'>密码修改通知</h2>" +
            "<p style='color: #666;'>亲爱的 <strong>%s</strong> 用户：</p>" +
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
        mailService.sendHtmlMail(student.getEmail(), subject, htmlContent);
        
        return true;
    }
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
}