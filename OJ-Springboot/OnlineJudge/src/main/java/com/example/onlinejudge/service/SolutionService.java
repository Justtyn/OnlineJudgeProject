package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Solution;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SolutionService extends IService<Solution> {

    /**
     * 添加题解
     *
     * @param solution 题解信息
     * @return 是否添加成功
     */
    boolean addSolution(Solution solution);

    /**
     * 删除题解
     *
     * @param id 题解ID
     * @return 是否删除成功
     */
    boolean deleteSolution(Integer id);

    /**
     * 分页查询题解
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 题解分页数据
     */
    Page<Solution> getSolutionPage(Integer current, Integer size);

    /**
     * 根据ID查询题解
     *
     * @param id 题解ID
     * @return 题解信息
     */
    Solution getSolutionById(Integer id);

    /**
     * 增加题解点赞数
     *
     * @param id 题解ID
     * @return 是否点赞成功
     */
    boolean increaseLike(Integer id);

    /**
     * 根据用户ID查询题解
     *
     * @param userId  用户ID
     * @param current 当前页
     * @param size    每页大小
     * @return 题解分页数据
     */
    Page<Solution> getSolutionsByUserId(Integer userId, Integer current, Integer size);

    /**
     * 获取题解总数量
     *
     * @return 题解总数
     */
    long getSolutionCount();

    // 获取题解总数
    long count();

    /**
     * 获取题解统计数据
     *
     * @return 包含各种统计数据的Map
     */
    Map<String, Object> getSolutionStats();

    /**
     * 获取题解点赞数分布
     *
     * @return 不同点赞数范围的题解数量
     */
    Map<String, Long> getLikeCountDistribution();

    /**
     * 获取最受欢迎的题解
     *
     * @param limit 获取数量
     * @return 最受欢迎的题解列表
     */
    List<Map<String, Object>> getMostLikedSolutions(int limit);

    /**
     * 编辑题解内容
     *
     * @param id      题解ID
     * @param content 新的题解内容
     * @return 是否编辑成功
     */
    boolean editSolution(Integer id, String content);

    /**
     * 获取题解语言分布
     *
     * @return 不同编程语言的使用数量
     */
    Map<String, Long> getLanguageDistribution();

    // 获取指定时间段内的新增题解数
    long getNewSolutionCount(LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户题解贡献排名
     *
     * @param limit 获取数量
     * @return 用户贡献排名列表
     */
    List<Map<String, Object>> getUserContributionRanking(int limit);

    /**
     * 获取题解质量分布
     *
     * @return 不同质量等级的题解数量
     */
    Map<String, Long> getQualityDistribution();

    /**
     * 获取每日题解趋势
     *
     * @param days 统计天数
     * @return 每日新增题解数量
     */
    List<Map<String, Object>> getDailySolutionTrend(int days);

    /**
     * 获取题解与问题的关联分析
     *
     * @return 问题-题解关联统计数据
     */
    Map<String, Object> getProblemSolutionAnalysis();

    /**
     * 获取题解长度分布
     *
     * @return 不同长度范围的题解数量
     */
    Map<String, Long> getContentLengthDistribution();

    /**
     * 获取题解创建时间分布
     *
     * @return 不同时间段的题解数量
     */
    Map<String, Long> getCreateTimeDistribution();

    /**
     * 获取最活跃的时间段
     *
     * @return 每个小时的题解创建数量
     */
    List<Map<String, Object>> getHourlyDistribution();

    /**
     * 获取题解质量趋势
     *
     * @param days 统计天数
     * @return 每日平均点赞数和浏览数
     */
    List<Map<String, Object>> getQualityTrend(int days);
}
