package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Teacher;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * TeacherMapper 接口
 * 该接口负责处理与教师(Teacher)实体相关的数据库操作
 * 使用MyBatis注解方式定义SQL语句
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 根据用户名查询教师信息
     *
     * @param username 用户名
     * @return 返回匹配的教师对象，如果不存在则返回null
     */
    @Select("select * from teacher where username = #{username}")
    Teacher selectByUsername(String username);

    /**
     * 插入新的教师记录
     *
     * @param teacher 要插入的教师对象，包含完整的教师信息
     */
    @Insert("insert into teacher (username,password,name,sex,phone,email,avatar,role,class_id) " +
            "values (#{username},#{password},#{name},#{sex},#{phone},#{email},#{avatar},#{role},#{classId})")
    int insert(Teacher teacher);

    /**
     * 根据ID查询教师信息
     *
     * @param id 教师ID
     * @return 返回匹配的教师对象，如果不存在则返回null
     */
    @Select("select * from teacher where id = #{id}")
    Teacher selectById(Integer id);

    /**
     * 查询所有教师信息
     *
     * @return 返回包含所有教师对象的列表
     */
    @Select("select * from teacher")
    List<Teacher> selectAll();

    /**
     * 更新教师信息
     *
     * @param teacher 包含更新信息的教师对象，必须包含id字段
     * @return 返回受影响的行数，通常成功更新时返回1
     */
    @Update("update teacher set username=#{username}, name=#{name}, sex=#{sex}, " +
            "phone=#{phone}, email=#{email}, avatar=#{avatar}, " +
            "password=#{password}, class_id=#{classId}, role=#{role} " +
            "where id=#{id}")
    int update(Teacher teacher);

    /**
     * 根据ID删除教师记录
     *
     * @param id 要删除的教师ID
     * @return 返回受影响的行数，通常成功删除时返回1
     */
    @Delete("delete from teacher where id = #{id}")
    int deleteById(Integer id);

    /**
     * 统计教师总数
     */
    @Select("SELECT COUNT(*) FROM teacher")
    Integer countAll();

    /**
     * 分页查询教师列表
     */
    @Select("SELECT * FROM teacher ORDER BY id DESC LIMIT #{offset}, #{limit}")
    List<Teacher> selectWithPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据用户名模糊查询教师信息
     *
     * @param username 用户名关键字
     * @return 返回匹配的教师对象列表
     */
    @Select("SELECT * FROM teacher WHERE username LIKE CONCAT('%', #{username}, '%')")
    List<Teacher> selectByUsernameLike(@Param("username") String username);

    /**
     * 根据姓名模糊查询教师信息
     *
     * @param name 姓名关键字
     * @return 返回匹配的教师对象列表
     */
    @Select("SELECT * FROM teacher WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Teacher> selectByNameLike(@Param("name") String name);

    /**
     * 根据创建时间的年份查询教师信息
     *
     * @param year 年份，如"2023"
     * @return 返回匹配的教师对象列表
     */
    @Select("SELECT * FROM teacher WHERE YEAR(create_time) = #{year}")
    List<Teacher> selectByCreateTimeYear(@Param("year") Integer year);

    /**
     * 根据班级ID查询教师信息
     *
     * @param classId 班级ID
     * @return 返回匹配的教师对象列表
     */
    @Select("SELECT * FROM teacher WHERE class_id = #{classId}")
    List<Teacher> selectByClassId(@Param("classId") Integer classId);

    /**
     * 获取指定时间段内新注册的教师数
     */
    @Select("SELECT COUNT(*) FROM teacher " +
            "WHERE create_time >= #{startDate} AND create_time <= #{endDate}")
    long countNewTeachers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
