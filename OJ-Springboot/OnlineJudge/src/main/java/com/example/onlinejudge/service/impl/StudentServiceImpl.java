package com.example.onlinejudge.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.StudentMapper;
import com.example.onlinejudge.service.StudentService;
import com.example.onlinejudge.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl implements StudentService {
    private static final String PASS_SALT = "OnlineJudge";

    @Resource
    private StudentMapper studentMapper;

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
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);
        String avatarUrl = "http://localhost:9090/uploads/" + fileName;
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
}