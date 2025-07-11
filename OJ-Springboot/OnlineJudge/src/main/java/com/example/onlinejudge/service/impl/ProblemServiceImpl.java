package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Problem;
import com.example.onlinejudge.mapper.ProblemMapper;
import com.example.onlinejudge.service.ProblemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {
    
    @Resource
    private ProblemMapper problemMapper;

    @Override
    public long count() {
        return problemMapper.selectTotal();
    }

    @Override
    public Map<String, Long> getDifficultyDistribution() {
        return problemMapper.selectDifficultyDistribution();
    }

    @Override
    public Map<String, Double> getAvgPassRateByDifficulty() {
        return problemMapper.selectAvgPassRateByDifficulty();
    }

    @Override
    public List<Map<String, Object>> getHardestProblems(int limit) {
        return problemMapper.selectHardestProblems(limit);
    }

    @Override
    public Map<String,Object> selectPage(Integer pageNum, Integer pageSize) {
        // 1. 偏移量 offset = (pageNum - 1) * pageSize
        int offset = (pageNum - 1) * pageSize;
        // 2. 自定义分页查询
        List<Problem> records = problemMapper.selectPage(offset, pageSize);
        // 3. 自定义总数
        long total = problemMapper.selectTotal();
    
        // 4. 组装返回
        Map<String,Object> m = new HashMap<>();
        m.put("list", records);
        m.put("total", total);
        m.put("pages", (total + pageSize - 1) / pageSize);
        m.put("current", pageNum);
        m.put("size", pageSize);
        return m;
    }
    
    

    @Override
    public Problem selectById(Integer id) {
        return problemMapper.selectById(id);
    }

    @Override
    public List<Problem> selectAll() {
        return problemMapper.selectAll();
    }

    @Override
    public void incrementSubmitCount(Integer id) {
        problemMapper.incrementSubmitCount(id);
    }

    @Override
    public void incrementAcCount(Integer id) {
        problemMapper.incrementAcCount(id);
    }

    @Override
    public Map<String, Object> selectByName(String name, Integer pageNum, Integer pageSize) {
        List<Problem> problems = problemMapper.selectByName(name, pageNum, pageSize);
        Long total = problemMapper.selectTotalByName(name);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", problems);
        resultMap.put("total", total);
        resultMap.put("pages", (total + pageSize - 1) / pageSize);
        resultMap.put("current", pageNum);
        resultMap.put("size", pageSize);
        
        return resultMap;
    }

    @Override
    public Map<String, Object> selectBySetter(String setter, Integer pageNum, Integer pageSize) {
        List<Problem> problems = problemMapper.selectBySetter(setter, pageNum, pageSize);
        Long total = problemMapper.selectTotalBySetter(setter);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", problems);
        resultMap.put("total", total);
        resultMap.put("pages", (total + pageSize - 1) / pageSize);
        resultMap.put("current", pageNum);
        resultMap.put("size", pageSize);
        
        return resultMap;
    }

    @Override
    public void add(Problem problem) {
        problemMapper.insert(problem);
    }

    @Override
    public boolean update(Problem problem) {
        problemMapper.update(problem);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) {
        problemMapper.deleteById(id);
        return true;
    }

    @Override
    public List<Problem> selectAllSimple() {
        return problemMapper.selectAllSimple();
    }

    @Override
    public List<Problem> getDailyChallenge() {
        return problemMapper.selectCurrentDailyChallenge();
    }

    @Override
    public List<Problem> searchByKeyword(String keyword) {
        return problemMapper.selectByKeyword(keyword);
    }

    @Override
    public boolean resetDailyChallengeProblems() {
        try {
            // 1. 重置所有题目的每日挑战状态为FALSE
            problemMapper.resetDailyChallengeStatus();
            
            // 2. 随机选择10道题目设置为每日挑战
            List<Problem> randomProblems = problemMapper.selectDailyChallenge();
            for (Problem problem : randomProblems) {
                problemMapper.updateDailyChallengeStatus(problem.getId(), "TRUE");
            }
            return true;
        } catch (Exception e) {
            log.error("重置每日挑战题目失败", e);
            return false;
        }
    }
} 