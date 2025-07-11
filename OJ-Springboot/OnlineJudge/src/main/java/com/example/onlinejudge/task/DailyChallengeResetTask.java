package com.example.onlinejudge.task;

import com.example.onlinejudge.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 每日挑战重置定时任务
 * 每天0点自动重置所有学生的每日挑战状态
 */
@Slf4j
@Component
public class DailyChallengeResetTask {

    @Resource
    private StudentService studentService;

    /**
     * 每天0点执行重置操作
     * cron表达式：0 0 0 * * ? 表示每天0点0分0秒执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyChallenge() {
        log.info("开始执行每日挑战状态重置任务");
        try {
            boolean success = studentService.resetAllDailyChallenge();
            if (success) {
                log.info("每日挑战状态重置成功");
            } else {
                log.error("每日挑战状态重置失败");
            }
        } catch (Exception e) {
            log.error("每日挑战状态重置过程中发生错误", e);
        }
    }
} 