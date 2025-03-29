package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Admin;
import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员数据访问接口
 * 该接口负责处理与管理员(Admin)相关的数据库操作
 * 使用MyBatis注解方式定义SQL语句
 */
public interface AdminMapper {
    /**
     * 根据用户名查询管理员信息
     * 主要用于管理员登录验证
     * 
     * @param username 管理员用户名
     * @return 返回查询到的管理员对象，如果不存在则返回null
     */
    @Select("select * from admin where username = #{username}")
    Admin selectByUsername(String username);

    /**
     * 插入新的管理员记录
     * 用于管理员注册或由超级管理员添加新管理员
     * 
     * @param admin 包含管理员完整信息的Admin对象，包括：
     *              - username: 用户名
     *              - password: 密码
     *              - name: 姓名
     *              - sex: 性别
     *              - birth: 出生日期
     *              - phone: 电话号码
     *              - email: 电子邮箱
     *              - avatar: 头像路径
     *              - role: 角色权限
     */
    @Insert("insert into admin (username,password,name,sex,birth,phone,email,avatar,role) " +
            "value (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role})")
    void insert(Admin admin);
}
