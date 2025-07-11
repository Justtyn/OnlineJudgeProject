package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Status;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatusMapper extends BaseMapper<Status> {

        @Select("SELECT status, COUNT(*) as count FROM oj_status GROUP BY status")
        List<Map<String, Object>> selectStatusDistribution();

        @Select("SELECT language, COUNT(*) as count FROM oj_status GROUP BY language")
        List<Map<String, Object>> selectLanguageDistribution();

        @Select("SELECT DATE(creat_time) as date, COUNT(*) as count " +
                        "FROM oj_status " +
                        "WHERE creat_time >= #{date} " +
                        "GROUP BY DATE(creat_time) " +
                        "ORDER BY date")
        List<Map<String, Object>> selectDailySubmissionTrend(
                        @Param("date") LocalDate date);

        @Select("SELECT COUNT(*) FROM oj_status " +
                        "WHERE creat_time BETWEEN #{startDate} AND #{endDate}")
        long countSubmissions(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}