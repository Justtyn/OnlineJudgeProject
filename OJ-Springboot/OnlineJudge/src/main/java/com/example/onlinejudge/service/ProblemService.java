package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Problem;

import java.util.List;
import java.util.Map;

public interface ProblemService extends IService<Problem> {
    // ... other method declarations ...

    long count();

    Map<String, Long> getDifficultyDistribution();

    Map<String, Double> getAvgPassRateByDifficulty();

    List<Map<String, Object>> getHardestProblems(int limit);

    Map<String, Object> selectPage(Integer pageNum, Integer pageSize);

    Problem selectById(Integer id);

    Map<String, Object> selectByName(String name, Integer pageNum, Integer pageSize);

    Map<String, Object> selectBySetter(String setter, Integer pageNum, Integer pageSize);

    void add(Problem problem);

    boolean update(Problem problem);

    boolean deleteById(Integer id);

    void incrementSubmitCount(Integer id);

    void incrementAcCount(Integer id);

    List<Problem> selectAll();

    List<Problem> selectAllSimple();

    List<Problem> getDailyChallenge();

    List<Problem> searchByKeyword(String keyword);

    /**
     * 重置所有题目的每日挑战状态并随机选择10道题目
     *
     * @return 是否重置成功
     */
    boolean resetDailyChallengeProblems();
} 