package com.example.onlinejudge.task;

import com.example.onlinejudge.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 每日题目重置定时任务
 * 每天0点自动重置所有题目的每日挑战状态并随机选择10道题目
 */
@Slf4j
@Component
public class DailyProblemResetTask {

    @Resource
    private ProblemService problemService;

    /**
     * 每天0点执行重置操作
     * cron表达式：0 0 0 * * ? 表示每天0点0分0秒执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyProblems() {
        log.info("开始执行每日题目重置任务");
        try {
            boolean success = problemService.resetDailyChallengeProblems();
            if (success) {
                log.info("每日题目重置成功");
            } else {
                log.error("每日题目重置失败");
            }
        } catch (Exception e) {
            log.error("每日题目重置过程中发生错误", e);
        }
    }
} 