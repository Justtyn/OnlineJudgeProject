package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * StudentMapper 接口
 * 该接口负责处理与学生(Student)实体相关的数据库操作
 * 使用MyBatis注解方式定义SQL语句
 */
public interface StudentMapper {

    /**
     * 根据用户名查询学生信息
     * @param username 用户名
     * @return 返回匹配的学生对象，如果不存在则返回null
     */
    @Select("select * from student where username = #{username}")
    Student selectByUsername(String username);

    /**
     * 插入新的学生记录
     * @param student 要插入的学生对象，包含完整的学生信息
     */
    @Insert("insert into student (username,password,name,sex,birth,phone,email,avatar,role,ac,submit) " +
            "value (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role},0,0)")
    void insert(Student student);
    
    /**
     * 根据ID查询学生信息
     * @param id 学生ID
     * @return 返回匹配的学生对象，如果不存在则返回null
     */
    @Select("select * from student where id = #{id}")
    Student selectById(Integer id);
    
    /**
     * 查询所有学生信息
     * @return 返回包含所有学生对象的列表
     */
    @Select("select * from student")
    List<Student> selectAll();
    
    /**
     * 更新学生信息
     * @param student 包含更新信息的学生对象，必须包含id字段
     * @return 返回受影响的行数，通常成功更新时返回1
     */
    @Update("update student set username=#{username}, name=#{name}, sex=#{sex}, " +
            "birth=#{birth}, phone=#{phone}, email=#{email}, avatar=#{avatar}, " +
            "ac=#{ac}, submit=#{submit} " +
            "where id=#{id}")
    int update(Student student);

    /**
     * 更新学生的背景图片
     * @param id 学生ID
     * @param background 新的背景图片路径或URL
     * @return 返回受影响的行数，通常成功更新时返回1
     */
    @Update("update student set background = #{background} where id = #{id}")
    int updateBackground(@Param("id") Integer id, @Param("background") String background);

    /**
     * 根据ID删除学生记录
     * @param id 要删除的学生ID
     * @return 返回受影响的行数，通常成功删除时返回1
     */
    @Delete("delete from student where id = #{id}")
    int deleteById(Integer id);
}
