package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Solution;
import com.example.onlinejudge.mapper.SolutionMapper;
import com.example.onlinejudge.service.SolutionService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import java.util.HashMap;

@Service
@Slf4j
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution> implements SolutionService {

    @Resource
    private SolutionMapper solutionMapper;

    @Override
    public boolean addSolution(Solution solution) {
        solution.setCreateTime(LocalDateTime.now());
        solution.setLikeNum(0);
        return save(solution);
    }

    @Override
    public boolean deleteSolution(Integer id) {
        return removeById(id);
    }

    @Override
    public Page<Solution> getSolutionPage(Integer current, Integer size) {
        Page<Solution> page = new Page<>(current, size);
        LambdaQueryWrapper<Solution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Solution::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public Solution getSolutionById(Integer id) {
        return getById(id);
    }

    @Override
    public boolean increaseLike(Integer id) {
        return baseMapper.increaseLike(id) > 0;
    }

    @Override
    public Page<Solution> getSolutionsByUserId(Integer userId, Integer current, Integer size) {
        Page<Solution> page = new Page<>(current, size);
        LambdaQueryWrapper<Solution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Solution::getUserId, userId);
        queryWrapper.orderByDesc(Solution::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public long count() {
        return solutionMapper.selectCount(null);
    }

    @Override
    public Map<String, Object> getSolutionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总题解数
        stats.put("totalSolutions", count());
        
        // 获取总点赞数
        stats.put("totalLikes", solutionMapper.getTotalLikes());
        
        // 获取总贡献用户数
        stats.put("totalContributors", solutionMapper.getTotalContributors());
        
        // 获取今日新增题解数
        stats.put("todayNewSolutions", solutionMapper.getTodayNewSolutions());
        
        return stats;
    }

    @Override
    public Map<String, Long> getLikeCountDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = solutionMapper.selectLikeCountDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getMostLikedSolutions(int limit) {
        return solutionMapper.selectMostLikedSolutions(limit);
    }

    @Override
    public Map<String, Long> getLanguageDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = solutionMapper.selectLanguageDistribution();
        for (Map<String, Object> data : rawData) {
            String language = (String) data.get("language");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(language, count);
        }
        
        return distribution;
    }

    @Override
    public long getNewSolutionCount(LocalDate startDate, LocalDate endDate) {
        return solutionMapper.countNewSolutions(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUserContributionRanking(int limit) {
        return solutionMapper.selectUserContributionRanking(limit);
    }

    @Override
    public Map<String, Long> getQualityDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = solutionMapper.selectQualityDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getDailySolutionTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return solutionMapper.selectDailySolutionTrend(startDate);
    }

    @Override
    public Map<String, Object> getProblemSolutionAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // 获取每个问题的题解数量分布
        analysis.put("solutionCountPerProblem", solutionMapper.selectSolutionCountPerProblem());
        
        // 获取最受关注的问题（题解数最多）
        analysis.put("mostSolvedProblems", solutionMapper.selectMostSolvedProblems(10));
        
        // 获取高质量题解最多的问题
        analysis.put("highQualitySolutionProblems", solutionMapper.selectHighQualitySolutionProblems(10));
        
        return analysis;
    }

    @Override
    public Map<String, Long> getContentLengthDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = solutionMapper.selectContentLengthDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public Map<String, Long> getCreateTimeDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = solutionMapper.selectCreateTimeDistribution();
        for (Map<String, Object> data : rawData) {
            String period = (String) data.get("period");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(period, count);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getHourlyDistribution() {
        return solutionMapper.selectHourlyDistribution();
    }

    @Override
    public List<Map<String, Object>> getQualityTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return solutionMapper.selectQualityTrend(startDate);
    }

    @Override
    public long getSolutionCount() {
        return count();
    }
}
