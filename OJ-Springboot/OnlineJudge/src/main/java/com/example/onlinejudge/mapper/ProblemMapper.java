package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Problem;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ProblemMapper {

    @Select("SELECT * FROM oj_problem ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectPage(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM oj_problem")
    Long selectTotal();

    @Select("SELECT * FROM oj_problem WHERE id = #{id}")
    Problem selectById(Integer id);

    @Select("SELECT * FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectByName(@Param("name") String name, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%')")
    Long selectTotalByName(String name);

    @Select("SELECT * FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectBySetter(@Param("setter") String setter, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%')")
    Long selectTotalBySetter(String setter);

    @Insert("INSERT INTO oj_problem(name, setter, create_time, ac_count, submit_count, `desc`, desc_input, desc_output, sample_input, sample_output, hint) " +
            "VALUES(#{name}, #{setter}, #{createTime}, #{acCount}, #{submitCount}, #{desc}, #{descInput}, #{descOutput}, #{sampleInput}, #{sampleOutput}, #{hint})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Problem problem);

    @Update("UPDATE oj_problem SET name = #{name}, setter = #{setter}, create_time = #{createTime}, " +
            "ac_count = #{acCount}, submit_count = #{submitCount}, `desc` = #{desc}, " +
            "desc_input = #{descInput}, desc_output = #{descOutput}, " +
            "sample_input = #{sampleInput}, sample_output = #{sampleOutput}, hint = #{hint} " +
            "WHERE id = #{id}")
    void update(Problem problem);

    @Delete("DELETE FROM oj_problem WHERE id = #{id}")
    void deleteById(Integer id);
} 