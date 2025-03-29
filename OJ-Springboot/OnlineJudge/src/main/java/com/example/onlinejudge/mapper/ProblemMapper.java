package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Problem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ProblemMapper 接口
 * 负责处理与题目(Problem)相关的数据库操作
 * 使用 MyBatis 注解方式定义 SQL 语句
 */
public interface ProblemMapper {

    /**
     * 分页查询所有题目
     * 
     * @param pageNum 页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回指定页的题目列表
     */
    @Select("SELECT * FROM oj_problem ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectPage(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 查询题目总数
     * 
     * @return 返回题目总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem")
    Long selectTotal();

    /**
     * 根据ID查询题目
     * 
     * @param id 题目ID
     * @return 返回指定ID的题目
     */
    @Select("SELECT * FROM oj_problem WHERE id = #{id}")
    Problem selectById(Integer id);

    /**
     * 根据题目名称模糊查询并分页
     * 
     * @param name 题目名称(支持模糊查询)
     * @param pageNum 页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回符合条件的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectByName(@Param("name") String name, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 根据题目名称查询符合条件的总记录数
     * 
     * @param name 题目名称(支持模糊查询)
     * @return 返回符合条件的记录总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%')")
    Long selectTotalByName(String name);

    /**
     * 根据出题人模糊查询并分页
     * 
     * @param setter 出题人(支持模糊查询)
     * @param pageNum 页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回符合条件的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectBySetter(@Param("setter") String setter, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 根据出题人查询符合条件的总记录数
     * 
     * @param setter 出题人(支持模糊查询)
     * @return 返回符合条件的记录总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%')")
    Long selectTotalBySetter(String setter);

    /**
     * 插入新题目
     * 使用 @Options 注解获取自动生成的主键值并赋值给 problem 对象的 id 属性
     * 
     * @param problem 要插入的题目对象
     */
    @Insert("INSERT INTO oj_problem(name, setter, create_time, ac_count, submit_count, `desc`, desc_input, desc_output, sample_input, sample_output, hint) " +
            "VALUES(#{name}, #{setter}, #{createTime}, #{acCount}, #{submitCount}, #{desc}, #{descInput}, #{descOutput}, #{sampleInput}, #{sampleOutput}, #{hint})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Problem problem);

    /**
     * 更新题目信息
     * 
     * @param problem 包含更新信息的题目对象
     */
    @Update("UPDATE oj_problem SET name = #{name}, setter = #{setter}, create_time = #{createTime}, " +
            "ac_count = #{acCount}, submit_count = #{submitCount}, `desc` = #{desc}, " +
            "desc_input = #{descInput}, desc_output = #{descOutput}, " +
            "sample_input = #{sampleInput}, sample_output = #{sampleOutput}, hint = #{hint} " +
            "WHERE id = #{id}")
    void update(Problem problem);

    /**
     * 根据ID删除题目
     * 
     * @param id 要删除的题目ID
     */
    @Delete("DELETE FROM oj_problem WHERE id = #{id}")
    void deleteById(Integer id);
} 