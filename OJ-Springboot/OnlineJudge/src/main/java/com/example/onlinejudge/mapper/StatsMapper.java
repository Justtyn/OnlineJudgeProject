package com.example.onlinejudge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计数据 Mapper
 */
@Mapper
public interface StatsMapper {

    /**
     * 1. 注册趋势：近 days 天每天注册数
     */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM student " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY DATE(create_time)")
    List<Map<String, Object>> selectRegistrationTrend(@Param("days") int days);

    /**
     * 2. 班级学生分布：班级名 + 人数
     */
    @Select("SELECT sc.name AS className, COUNT(*) AS count " +
            "FROM student st " +
            "LEFT JOIN score sc ON st.class_id = sc.id " +
            "GROUP BY sc.id, sc.name")
    List<Map<String, Object>> selectClassDistribution();

    /**
     * 3. 题目提交排行：按 submit_count 降序 LIMIT #{limit}
     */
    @Select("SELECT id, name AS problemName, submit_count AS submitCount " +
            "FROM oj_problem " +
            "ORDER BY submit_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopProblemsBySubmit(@Param("limit") int limit);

    /**
     * 4. 提交状态分布：不同状态的提交数
     */
    @Select("SELECT status, COUNT(*) AS count " +
            "FROM oj_status " +
            "GROUP BY status")
    List<Map<String, Object>> selectStatusDistribution();
}