package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface StudentMapper {

    @Select("select * from student where username = #{username}")
    Student selectByUsername(String username);

    @Insert("insert into student (username,password,name,sex,birth,phone,email,avatar,role) " +
            "value (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role})")
    void insert(Student student);
}
