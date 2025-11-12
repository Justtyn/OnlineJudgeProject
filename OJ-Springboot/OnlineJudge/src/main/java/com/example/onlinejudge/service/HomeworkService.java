package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Homework;

import java.util.List;
import java.util.Map;

public interface HomeworkService extends IService<Homework> {
    // 添加作业
    boolean addHomework(Homework homework);

    // 删除作业
    boolean deleteHomework(Integer id);

    // 分页查询所有作业
    Page<Homework> listHomeworkByPage(Integer current, Integer size);

    // 分页查询作业（支持搜索和筛选）
    Page<Homework> listHomeworkByPage(Integer current, Integer size, String title, Integer classId, String status);

    // 按classId查询作业
    List<Homework> listHomeworkByClassId(Integer classId);

    // 更新作业
    boolean updateHomework(Homework homework);

    /**
     * 获取作业总数
     */
    long count();

    /**
     * 获取作业提交情况统计
     *
     * @return 包含各种统计数据的Map
     */
    Map<String, Object> getHomeworkStats();

    /**
     * 获取作业完成率分布
     *
     * @return 不同完成率范围的作业数量
     */
    Map<String, Long> getCompletionRateDistribution();

    /**
     * 获取作业难度分布
     *
     * @return 不同难度的作业数量
     */
    Map<String, Long> getDifficultyDistribution();

    /**
     * 获取最近一周作业提交趋势
     *
     * @return 每日提交数量列表
     */
    List<Map<String, Object>> getWeeklySubmissionTrend();

    /**
     * 获取课程作业统计
     *
     * @return 各课程的作业统计信息
     */
    List<Map<String, Object>> getCourseHomeworkStats();

    Map<String, Object> getSubmissionRateDistribution();

    Map<String, Object> getAvgCompletionTimeDistribution();
}
