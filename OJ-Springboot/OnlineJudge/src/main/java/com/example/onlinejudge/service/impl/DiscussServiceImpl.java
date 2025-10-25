package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Discuss;
import com.example.onlinejudge.mapper.DiscussMapper;
import com.example.onlinejudge.service.DiscussService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class DiscussServiceImpl extends ServiceImpl<DiscussMapper, Discuss> implements DiscussService {
    
    @Resource
    private DiscussMapper discussMapper;

    @Override
    public long count() {
        return discussMapper.selectCount(null);
    }

    @Override
    public Map<String, Object> getDiscussStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总讨论数
        stats.put("totalDiscussions", discussMapper.selectTotal());
        
        // 获取总评论数
        stats.put("totalComments", discussMapper.getTotalComments());
        
        // 获取总参与用户数
        stats.put("totalParticipants", discussMapper.getTotalParticipants());
        
        // 获取今日新增讨论数
        stats.put("todayNewDiscussions", discussMapper.getTodayNewDiscussions());
        
        return stats;
    }

    @Override
    public Map<String, Long> getTopicDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = discussMapper.selectTopicDistribution();
        for (Map<String, Object> data : rawData) {
            String topic = (String) data.get("topic");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(topic, count);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getDailyDiscussionTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return discussMapper.selectDailyDiscussionTrend(startDate);
    }

    @Override
    public List<Map<String, Object>> getDailyDiscussionTrend() {
        // 默认获取最近7天的趋势
        return getDailyDiscussionTrend(7);
    }

    @Override
    public List<Map<String, Object>> getHotDiscussions(int limit) {
        return discussMapper.selectHotDiscussions(limit);
    }

    @Override
    public List<Map<String, Object>> getHotDiscussions() {
        // 默认获取前10条热门讨论
        return getHotDiscussions(10);
    }

    @Override
    public long getNewDiscussionCount(LocalDate startDate, LocalDate endDate) {
        return discussMapper.countNewDiscussions(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUserParticipationRanking(int limit) {
        return discussMapper.selectUserParticipationRanking(limit);
    }

    @Override
    public List<Map<String, Object>> getUserParticipationRanking() {
        // 默认获取前10名用户的参与度排名
        return getUserParticipationRanking(10);
    }

    @Override
    public Map<String, Long> getReplyTimeDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = discussMapper.selectReplyTimeDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public Map<String, Object> selectPage(Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 获取分页数据和总数
        List<Discuss> list = discussMapper.selectPage(offset, pageSize);
        Long total = discussMapper.selectTotal();
        
        result.put("list", list);
        result.put("total", total);
        
        return result;
    }

    @Override
    public Discuss selectById(Integer id) {
        // 获取讨论详情
        Discuss discuss = discussMapper.selectById(id);
        if (discuss != null) {
            // 增加浏览量
            discussMapper.incrementViewNum(id);
        }
        return discuss;
    }

    @Override
    public List<Discuss> selectByTitle(String title) {
        return discussMapper.selectByTitle(title);
    }

    @Override
    public List<Discuss> selectByProblemId(Integer problemId) {
        return discussMapper.selectByProblemId(problemId);
    }

    @Override
    public void add(Discuss discuss) {
        discussMapper.insert(discuss);
    }

    @Override
    public void update(Discuss discuss) {
        discussMapper.update(discuss);
    }

    @Override
    public void deleteById(Integer id) {
        discussMapper.deleteById(id);
    }

    @Override
    public List<Discuss> selectAll() {
        return discussMapper.selectAll();
    }
} 