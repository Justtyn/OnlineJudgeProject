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

/**
 * 管理员服务类
 * 处理管理员相关的业务逻辑，包括登录、注册等功能
 */
@Service
public class AdminService {
    // 密码加密用的盐值，用于增加密码安全性
    private static final String PASS_SALT = "OnlineJudge";

    // 注入管理员数据访问层
    @Resource
    private AdminMapper adminMapper;

    /**
     * 密码加密方法
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    /**
     * 管理员登录方法
     *
     * @param account 包含用户名和密码的账号对象
     * @return 登录成功后返回带有token的账号信息
     * @throws CustomException 当登录失败时抛出异常
     */
    @Transactional
    public Account login(Account account) {
        // 根据用户名查询数据库中的账号信息
        Account dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (dbAdmin == null) {
            // 如果数据库中没有找到该用户，抛出异常
            throw new CustomException("账号或密码错误");
        }
        // 比较密码是否匹配
        if (!securePass(account.getPassword()).equals(dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 登录成功，生成JWT token
        String token = JwtUtil.generateToken(dbAdmin.getUsername());
        dbAdmin.setToken(token);
        return dbAdmin;
    }

    /**
     * 管理员注册方法
     *
     * @param account 包含注册信息的账号对象
     */
    @Transactional
    public void register(Account account) {
        // 创建新的管理员对象
        Admin admin = new Admin();
        admin.setUsername(account.getUsername());
        admin.setPassword(account.getPassword());
        // 调用添加方法完成注册
        this.add(admin);
    }

    /**
     * 添加新管理员方法
     *
     * @param admin 管理员对象
     * @throws CustomException 当用户名已存在时抛出异常
     */
    public void add(Admin admin) {
        // 检查用户名是否已存在
        Admin dbStudent = adminMapper.selectByUsername(admin.getUsername());
        if (dbStudent != null) {
            throw new CustomException("账号已存在");
        }

        // 如果没有设置名称，则使用用户名作为名称
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        // 对密码进行加密
        admin.setPassword(SecureUtil.md5(admin.getPassword() + PASS_SALT));
        // 设置角色为学生（这里可能存在逻辑问题，因为是AdminService却设置STUDENT角色）
        admin.setRole(RoleEnum.ADMIN.name());

        // 将管理员信息插入数据库
        adminMapper.insert(admin);
    }

    /**
     * 根据ID查询管理员信息
     *
     * @param id 管理员ID
     * @return 管理员信息
     * @throws CustomException 当管理员不存在时抛出异常
     */
    public Admin getById(Integer id) {
        if (id == null) {
            throw new CustomException("管理员ID不能为空");
        }
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new CustomException("管理员不存在");
        }
        // 出于安全考虑，不返回密码
        admin.setPassword(null);
        return admin;
    }
}
