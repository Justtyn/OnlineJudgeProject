package com.example.onlinejudge.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Admin;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.AdminMapper;
import com.example.onlinejudge.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class AdminService {
    private static final String PASS_SALT = "OnlineJudge";

    @Resource
    private AdminMapper adminMapper;

    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    /**
     * 登陆
     */
    @Transactional
    public Account login(Account account) {
        Account dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (dbAdmin == null) {
            // 数据库没有用户
            throw new CustomException("账号或密码错误");
        }
        // 比较密码
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 登陆成功
        // 生成 token
        String token = JwtUtil.generateToken(dbAdmin.getUsername());
        dbAdmin.setToken(token);
        return dbAdmin;
    }


    // 注册方法
    @Transactional
    public void register(Account account) {
        Admin admin = new Admin();
        admin.setUsername(account.getUsername());
        admin.setPassword(account.getPassword());
        this.add(admin);
    }

    // 新增方法
    public void add(Admin admin) {
        Admin dbStudent = adminMapper.selectByUsername(admin.getUsername());
        if (dbStudent != null) {    // 已有同名账号，不允许插入
            throw new CustomException("账号已存在");
        }

        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setPassword(SecureUtil.md5(admin.getPassword() + PASS_SALT));
        admin.setRole(RoleEnum.STUDENT.name());

        adminMapper.insert(admin);
    }
}
