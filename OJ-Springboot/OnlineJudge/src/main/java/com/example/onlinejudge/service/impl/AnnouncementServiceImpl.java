package com.example.onlinejudge.service.impl;

import com.example.onlinejudge.entity.Announcement;
import com.example.onlinejudge.mapper.AnnouncementMapper;
import com.example.onlinejudge.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public Announcement getByTitle(String title) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Announcement::getTitle, title); // 使用like查询，更灵活
        List<Announcement> list = this.list(queryWrapper);

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Announcement getByContent(String content) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Announcement::getContent, content); // 使用like查询，更灵活
        List<Announcement> list = this.list(queryWrapper);

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /** “最近公告统计”里原来放 typeDistribution，就改成月度分布 */
    @Override
    public Map<String, Object> getRecentAnnouncementsStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("timeDistribution", getTimeDistribution());
        stats.put("monthlyDistribution", getTypeDistribution());
        return stats;
    }

    @Override
    public Map<String, Object> getTypeDistribution() {
        Map<String, Object> map = new LinkedHashMap<>();
        announcementMapper.selectMonthlyDistribution().forEach(row -> {
            String period = (String) row.get("period");
            Long cnt      = ((Number) row.get("cnt")).longValue();
            map.put(period, cnt);
        });
        return map;
    }
    
    @Override
    public Map<String, Object> getTimeDistribution() {
        Map<String, Object> map = new LinkedHashMap<>();
        announcementMapper.selectDailyDistribution().forEach(row -> {
            String period = (String) row.get("period");
            Long cnt      = ((Number) row.get("cnt")).longValue();
            map.put(period, cnt);
        });
        return map;
    }
}
