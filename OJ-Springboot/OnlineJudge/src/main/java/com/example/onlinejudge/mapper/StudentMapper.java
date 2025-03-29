package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface StudentMapper {

    @Select("select * from student where username = #{username}")
    Student selectByUsername(String username);

    @Insert("insert into student (username,password,name,sex,birth,phone,email,avatar,role) " +
            "value (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role})")
    void insert(Student student);
    
    @Select("select * from student where id = #{id}")
    Student selectById(Integer id);
    
    @Select("select * from student")
    List<Student> selectAll();
    
    @Update("update student set username=#{username}, name=#{name}, sex=#{sex}, " +
            "birth=#{birth}, phone=#{phone}, email=#{email}, avatar=#{avatar} " +
            "where id=#{id}")
    int update(Student student);
    
    @Delete("delete from student where id = #{id}")
    int deleteById(Integer id);
}
