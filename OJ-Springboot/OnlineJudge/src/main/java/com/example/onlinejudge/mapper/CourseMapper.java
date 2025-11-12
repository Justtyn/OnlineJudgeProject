package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 按学生数区间统计
     */
    @Select({
            "SELECT",
            "  CASE",
            "    WHEN t.student_count = 0   THEN '0'",
            "    WHEN t.student_count <= 10 THEN '1-10'",
            "    WHEN t.student_count <= 30 THEN '11-30'",
            "    WHEN t.student_count <= 50 THEN '31-50'",
            "    ELSE '50+'",
            "  END AS student_count_range,",
            "  COUNT(*) AS cnt",
            "FROM (",
            "  SELECT c.id, COUNT(s.id) AS student_count",
            "  FROM score c",
            "  LEFT JOIN student s ON c.id = s.class_id",
            "  GROUP BY c.id",
            ") AS t",
            "GROUP BY student_count_range"
    })
    List<Map<String, Object>> selectStudentCountDistribution();

    /**
     * 按作业数量区间统计
     */
    @Select({
            "SELECT",
            "  CASE",
            "    WHEN homework_quantity = 0   THEN '0'",
            "    WHEN homework_quantity <= 5  THEN '1-5'",
            "    WHEN homework_quantity <= 10 THEN '6-10'",
            "    ELSE '11+'",
            "  END AS homework_qty_range,",
            "  COUNT(*) AS cnt",
            "FROM score",
            "GROUP BY homework_qty_range"
    })
    List<Map<String, Object>> selectHomeworkCountDistribution();

    /**
     * 最活跃的 N 门课程
     */
    @Select({
            "SELECT",
            "  c.id, c.name, c.creator_id,",
            "  COUNT(DISTINCT s.id)  AS student_count,",
            "  COUNT(DISTINCT h.id)  AS homework_count,",
            "  COUNT(st.id)          AS submission_count",
            "FROM score c",
            "LEFT JOIN student s           ON c.id = s.class_id",
            "LEFT JOIN homework h          ON c.id = h.class_id",
            "LEFT JOIN homework_problem hp ON h.id = hp.homework_id",
            "LEFT JOIN oj_status st        ON st.problem_id = hp.problem_id",
            "GROUP BY c.id, c.name, c.creator_id",
            "ORDER BY submission_count DESC",
            "LIMIT #{limit}"
    })
    List<Map<String, Object>> selectMostActiveCourses(@Param("limit") int limit);

}
