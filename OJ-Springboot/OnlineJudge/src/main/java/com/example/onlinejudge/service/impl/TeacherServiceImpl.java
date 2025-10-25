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
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        // 实现头像上传逻辑
        // 这里可以复用StudentServiceImpl中的头像上传逻辑
        return "avatar_uploaded";
    }

    @Override
    public boolean resetPassword(String username, String email) {
        // 实现密码重置逻辑
        return true;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // 实现密码修改逻辑
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