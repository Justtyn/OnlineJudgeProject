package com.example.onlinejudge.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.StudentMapper;
import com.example.onlinejudge.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.beans.Transient;

@Service
public class StudentService {
    private static final String PASS_SALT = "OnlineJudge";
    @Resource
    private StudentMapper studentMapper;

    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    /**
     * 登陆
     */
    @Transactional
    public Account login(Account account) {
        Account dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (dbStudent == null) {
            // 数据库没有用户
            throw new CustomException("账号或密码错误");
        }
        // 比较密码
        if (!securePass(account.getPassword()).equals(dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 登陆成功
        // 生成 token
        String token = JwtUtil.generateToken(dbStudent.getUsername());
        dbStudent.setToken(token);
        return dbStudent;
    }

    // 注册方法
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

    // 新增方法
    public void add(Student student) {
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (dbStudent != null) {    // 已有同名账号，不允许插入
            throw new CustomException("账号已存在");
        }

        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setPassword(SecureUtil.md5(student.getPassword() + PASS_SALT));
        student.setRole(RoleEnum.STUDENT.name());


        studentMapper.insert(student);
    }
}
