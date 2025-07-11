// DiscussService.java
package com.example.onlinejudge.service;

import cn.hutool.core.date.DateUtil;
import com.example.onlinejudge.entity.Discuss;
import com.example.onlinejudge.exception.CustomException;
import java.time.LocalDate;
import java.util.*;

public interface DiscussService {

    /**
     * 分页倒序查询讨论列表
     * @param pageNum 当前页（1 起）
     * @param pageSize 每页数量
     * @return 包含 list 和 total 的 Map
     */
    Map<String, Object> selectPage(Integer pageNum, Integer pageSize);

    /**
     * 根据 ID 查询讨论详情并组装评论树，同时 +1 浏览量
     * @param id 讨论ID
     */
    Discuss selectById(Integer id);

    /**
     * 新增讨论
     * @param discuss 讨论实体
     */
    void add(Discuss discuss);

    /**
     * 修改讨论
     * @param discuss 包含 id 和要更新的字段
     */
    void update(Discuss discuss);

    /**
     * 删除讨论
     * @param id 讨论ID
     */
    void deleteById(Integer id);

    /**
     * 查询所有讨论（倒序，不分页）
     */
    List<Discuss> selectAll();

    /**
     * 根据标题模糊查询讨论列表
     * @param title 标题关键字
     */
    List<Discuss> selectByTitle(String title);

    // 获取讨论总数
    long count();

    // 获取讨论主题分布
    Map<String, Long> getTopicDistribution();

    // 获取每日新增讨论趋势
    List<Map<String, Object>> getDailyDiscussionTrend(int days);

    // 获取最热门的N个讨论
    List<Map<String, Object>> getHotDiscussions(int limit);

    // 获取指定时间段内的新增讨论数
    long getNewDiscussionCount(LocalDate startDate, LocalDate endDate);

    // 获取用户参与度排名
    List<Map<String, Object>> getUserParticipationRanking(int limit);

    /**
     * 获取讨论区统计数据
     * @return 包含各种统计数据的Map
     */
    Map<String, Object> getDiscussStats();

    /**
     * 获取每日讨论趋势
     * @param days 统计天数
     * @return 每日新增讨论数量
     */
    List<Map<String, Object>> getDailyDiscussionTrend();

    /**
     * 获取最热门讨论
     * @param limit 获取数量
     * @return 热门讨论列表
     */
    List<Map<String, Object>> getHotDiscussions();

    /**
     * 获取用户参与度排名
     * @param limit 获取数量
     * @return 用户参与度排名列表
     */
    List<Map<String, Object>> getUserParticipationRanking();

    /**
     * 获取讨论回复时间分布
     * @return 不同回复时间范围的数量
     */
    Map<String, Long> getReplyTimeDistribution();
}
