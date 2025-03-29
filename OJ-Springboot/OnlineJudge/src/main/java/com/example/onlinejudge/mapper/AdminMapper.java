package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Admin;
import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper {
    /**
     * 登陆
     */
    @Select("select * from admin where username = #{username}")
    Admin selectByUsername(String username);

    @Insert("insert into admin (username,password,name,sex,birth,phone,email,avatar,role) " +
            "value (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role})")
    void insert(Admin admin);
}
