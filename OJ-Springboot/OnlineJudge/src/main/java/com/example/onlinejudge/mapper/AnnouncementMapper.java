package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    // 日度分布
    @Select({
            "SELECT",
            "  DATE_FORMAT(`time`, '%Y-%m-%d') AS period,",
            "  COUNT(*)                       AS cnt",
            "FROM announcement",
            // 这两行改成下面这样，和 SELECT 用同样的表达式
            "GROUP BY DATE_FORMAT(`time`, '%Y-%m-%d')",
            "ORDER BY DATE_FORMAT(`time`, '%Y-%m-%d')"
    })
    List<Map<String, Object>> selectDailyDistribution();

    // 月度分布 —— 已经是 DATE_FORMAT(time, '%Y-%m')，无需改
    @Select({
            "SELECT",
            "  DATE_FORMAT(`time`, '%Y-%m') AS period,",
            "  COUNT(*)                     AS cnt",
            "FROM announcement",
            "GROUP BY DATE_FORMAT(`time`, '%Y-%m')",
            "ORDER BY DATE_FORMAT(`time`, '%Y-%m')"
    })
    List<Map<String, Object>> selectMonthlyDistribution();
} 