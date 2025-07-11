package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Solution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface SolutionMapper extends BaseMapper<Solution> {
    
    @Update("UPDATE solution SET `like_num` = `like_num` + 1 WHERE id = #{id}")
    int increaseLike(Integer id);

    @Select("SELECT SUM(like_num) FROM solution")
    Long getTotalLikes();

    @Select("SELECT COUNT(DISTINCT user_id) FROM solution")
    Long getTotalContributors();

    @Select("SELECT COUNT(*) FROM solution WHERE DATE(create_time) = CURDATE()")
    Long getTodayNewSolutions();

    @Select("SELECT " +
            "CASE " +
            "  WHEN like_num = 0 THEN '0' " +
            "  WHEN like_num <= 10 THEN '1-10' " +
            "  WHEN like_num <= 50 THEN '11-50' " +
            "  WHEN like_num <= 100 THEN '51-100' " +
            "  ELSE '100+' " +
            "END as range, " +
            "COUNT(*) as count " +
            "FROM solution " +
            "GROUP BY range")
    List<Map<String, Object>> selectLikeCountDistribution();

    @Select("SELECT s.id, s.title, " +
            "p.name as problem_name, " +
            "u.username as author_name, " +
            "s.like_num, s.view_num, " +
            "s.language, s.create_time " +
            "FROM solution s " +
            "LEFT JOIN oj_problem p ON s.problem_id = p.id " +
            "LEFT JOIN student u ON s.user_id = u.id " +
            "ORDER BY s.like_num DESC, s.view_num DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectMostLikedSolutions(int limit);

    @Select("SELECT language, COUNT(*) as count " +
            "FROM solution " +
            "GROUP BY language")
    List<Map<String, Object>> selectLanguageDistribution();

    @Select("SELECT u.username, " +
            "COUNT(s.id) as solution_count, " +
            "SUM(s.like_num) as total_likes, " +
            "AVG(s.like_num) as avg_likes " +
            "FROM student u " +
            "JOIN solution s ON u.id = s.user_id " +
            "GROUP BY u.id " +
            "ORDER BY solution_count DESC, total_likes DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectUserContributionRanking(int limit);

    @Select("SELECT " +
            "CASE " +
            "  WHEN quality_score <= 10 THEN '低' " +
            "  WHEN quality_score <= 50 THEN '中' " +
            "  ELSE '高' " +
            "END as range, " +
            "COUNT(*) as count " +
            "FROM (" +
            "  SELECT id, " +
            "  (like_num * 2 + view_num) as quality_score " +
            "  FROM solution" +
            ") t " +
            "GROUP BY range")
    List<Map<String, Object>> selectQualityDistribution();

    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as count " +
            "FROM solution " +
            "WHERE create_time >= #{startDate} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailySolutionTrend(LocalDate startDate);

    @Select("SELECT p.name as problem_name, " +
            "COUNT(s.id) as solution_count, " +
            "AVG(s.like_num) as avg_likes " +
            "FROM oj_problem p " +
            "LEFT JOIN solution s ON p.id = s.problem_id " +
            "GROUP BY p.id " +
            "ORDER BY solution_count DESC")
    List<Map<String, Object>> selectSolutionCountPerProblem();

    @Select("SELECT p.id, p.name, " +
            "COUNT(s.id) as solution_count " +
            "FROM oj_problem p " +
            "JOIN solution s ON p.id = s.problem_id " +
            "GROUP BY p.id " +
            "ORDER BY solution_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectMostSolvedProblems(int limit);

    @Select("SELECT p.id, p.name, " +
            "COUNT(s.id) as solution_count, " +
            "AVG(s.like_num) as avg_likes " +
            "FROM oj_problem p " +
            "JOIN solution s ON p.id = s.problem_id " +
            "GROUP BY p.id " +
            "HAVING COUNT(s.id) >= 5 " +  // 至少有5个题解
            "ORDER BY avg_likes DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHighQualitySolutionProblems(int limit);

    @Select("SELECT " +
            "CASE " +
            "  WHEN LENGTH(content) <= 500 THEN '短文(<=500)' " +
            "  WHEN LENGTH(content) <= 2000 THEN '中等(501-2000)' " +
            "  WHEN LENGTH(content) <= 5000 THEN '长文(2001-5000)' " +
            "  ELSE '超长文(>5000)' " +
            "END as range, " +
            "COUNT(*) as count " +
            "FROM solution " +
            "GROUP BY range")
    List<Map<String, Object>> selectContentLengthDistribution();

    @Select("SELECT " +
            "CASE " +
            "  WHEN DATEDIFF(NOW(), create_time) <= 7 THEN '最近一周' " +
            "  WHEN DATEDIFF(NOW(), create_time) <= 30 THEN '最近一月' " +
            "  WHEN DATEDIFF(NOW(), create_time) <= 90 THEN '最近三月' " +
            "  WHEN DATEDIFF(NOW(), create_time) <= 180 THEN '最近半年' " +
            "  ELSE '更早' " +
            "END as period, " +
            "COUNT(*) as count " +
            "FROM solution " +
            "GROUP BY period")
    List<Map<String, Object>> selectCreateTimeDistribution();

    @Select("SELECT HOUR(create_time) as hour, " +
            "COUNT(*) as count " +
            "FROM solution " +
            "GROUP BY hour " +
            "ORDER BY hour")
    List<Map<String, Object>> selectHourlyDistribution();

    @Select("SELECT DATE(create_time) as date, " +
            "AVG(like_num) as avg_likes, " +
            "AVG(view_num) as avg_views " +
            "FROM solution " +
            "WHERE create_time >= #{startDate} " +
            "GROUP BY date " +
            "ORDER BY date")
    List<Map<String, Object>> selectQualityTrend(LocalDate startDate);

    @Select("SELECT COUNT(*) FROM solution WHERE DATE(create_time) BETWEEN #{startDate} AND #{endDate}")
    long countNewSolutions(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT p.id, p.name, " +
            "COUNT(s.id) as solution_count, " +
            "AVG(s.like_num) as avg_likes " +
            "FROM oj_problem p " +
            "JOIN solution s ON p.id = s.problem_id " +
            "GROUP BY p.id " +
            "ORDER BY avg_likes DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectPopularProblems(int limit);
}
