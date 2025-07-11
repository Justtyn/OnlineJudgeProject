package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Student;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * StudentMapper 接口
 * 该接口负责处理与学生(Student)实体相关的数据库操作
 * 使用MyBatis注解方式定义SQL语句
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

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
    @Insert("insert into student (username,password,name,sex,birth,phone,email,avatar,role,ac,submit,school,background,class_id,daily_challenge) " +
            "values (#{username},#{password},#{name},#{sex},#{birth},#{phone},#{email},#{avatar},#{role},0,0,#{school},#{background},#{classId},'0')")
    int insert(Student student);
    
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
<<<<<<< HEAD
    "birth=#{birth}, phone=#{phone}, email=#{email}, avatar=#{avatar}, " +
    "ac=#{ac}, submit=#{submit}, password=#{password}, school=#{school}, " +
    "background=#{background}, class_id=#{classId}, daily_challenge=#{dailyChallenge}, " +
    "role=#{role}, last_login_time=#{lastLoginTime}, last_language=#{lastLanguage}, " +
    "last_visit_time=#{lastVisitTime} " +
    "where id=#{id}")
=======
            "birth=#{birth}, phone=#{phone}, email=#{email}, avatar=#{avatar}, " +
            "ac=#{ac}, submit=#{submit}, password=#{password} " +
            "where id=#{id}")
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
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
    
    /**
     * 根据AC数排序查询所有学生
     * @return 返回按AC数量降序排列的学生列表
     */
    @Select("select * from student order by ac desc")
    List<Student> selectAllOrderByAc();

    /**
     * 统计学生总数
     */
    @Select("SELECT COUNT(*) FROM student")
    Integer countAll();

    /**
     * 分页查询按AC数量降序排列的学生列表
     */
    @Select("SELECT * FROM student ORDER BY ac DESC LIMIT #{offset}, #{limit}")
    List<Student> selectOrderByAcWithPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据用户名模糊查询学生信息
     * @param username 用户名关键字
     * @return 返回匹配的学生对象列表
     */
    @Select("SELECT * FROM student WHERE username LIKE CONCAT('%', #{username}, '%')")
    List<Student> selectByUsernameLike(@Param("username") String username);

    /**
     * 根据姓名模糊查询学生信息
     * @param name 姓名关键字
     * @return 返回匹配的学生对象列表
     */
    @Select("SELECT * FROM student WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Student> selectByNameLike(@Param("name") String name);

    /**
     * 根据创建时间的年份查询学生信息
     * @param year 年份，如"2023"
     * @return 返回匹配的学生对象列表
     */
    @Select("SELECT * FROM student WHERE YEAR(create_time) = #{year}")
    List<Student> selectByCreateTimeYear(@Param("year") Integer year);

    /**
     * 更新学生的每日挑战状态
     * @param id 学生ID
     * @param dailyChallenge 每日挑战状态
     * @return 返回受影响的行数，通常成功更新时返回1
     */
    @Update("update student set daily_challenge = #{dailyChallenge} where id = #{id}")
    int updateDailyChallenge(@Param("id") Integer id, @Param("dailyChallenge") String dailyChallenge);

    /**
     * 重置所有学生的每日挑战状态为FALSE
     * @return 返回受影响的行数
     */
    @Update("UPDATE student SET daily_challenge = '0'")
    int resetAllDailyChallenge();

    @Select("SELECT COUNT(DISTINCT s.id) FROM student s " +
            "JOIN oj_status st ON s.id = st.user_id " +
            "WHERE st.creat_time >= #{date}")
    long countActiveStudents(LocalDate date);

    @Select("SELECT COUNT(*) FROM student WHERE ac >= #{min} AND ac <= #{max}")
    long countByAcRange(@Param("min") int min, @Param("max") int max);

    @Select("SELECT COUNT(*) FROM student WHERE submit >= #{min} AND submit <= #{max}")
    long countBySubmitRange(@Param("min") int min, @Param("max") int max);

    @Select("SELECT id, username, name, ac, submit, " +
            "CASE WHEN submit > 0 THEN ROUND(ac * 100.0 / submit, 2) ELSE 0 END as pass_rate " +
            "FROM student " +
            "ORDER BY pass_rate DESC, ac DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopPerformers(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM student " +
            "WHERE create_time >= #{startDate} AND create_time <= #{endDate}")
    long countNewUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
