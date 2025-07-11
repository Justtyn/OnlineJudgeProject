package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Course;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper extends BaseMapper<Course> {

    /**
     * 按作业数量区间统计
     */
    @Select(
      "SELECT\n" +
      "  CASE\n" +
      "    WHEN homework_quantity < 5  THEN '0-4'\n" +
      "    WHEN homework_quantity < 10 THEN '5-9'\n" +
      "    WHEN homework_quantity < 15 THEN '10-14'\n" +
      "    ELSE '15+'\n" +
      "  END AS homework_qty_range,\n" +
      "  COUNT(*) AS cnt\n" +
      "FROM score\n" +
      "GROUP BY homework_qty_range"
    )
    List<Map<String, Object>> selectHomeworkCountDistribution();
}
