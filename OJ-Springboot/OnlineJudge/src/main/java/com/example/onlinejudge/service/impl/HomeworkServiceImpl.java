package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Homework;
import com.example.onlinejudge.mapper.HomeworkMapper;
import com.example.onlinejudge.service.HomeworkService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements HomeworkService {
    
    @Autowired
    private HomeworkMapper homeworkMapper;
    
    @Override
    public boolean addHomework(Homework homework) {
        return homeworkMapper.insert(homework) > 0;
    }
    
    @Override
    public boolean deleteHomework(Integer id) {
        return homeworkMapper.deleteById(id) > 0;
    }
    
    @Override
    public Page<Homework> listHomeworkByPage(Integer current, Integer size) {
        Page<Homework> page = new Page<>(current, size);
        return homeworkMapper.selectPage(page, null);
    }
    
    @Override
    public List<Homework> listHomeworkByClassId(Integer classId) {
        QueryWrapper<Homework> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        return homeworkMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean updateHomework(Homework homework) {
        return homeworkMapper.updateById(homework) > 0;
    }

    @Override
    public long count() {
        return homeworkMapper.selectCount(null);
    }

    @Override
    public Map<String, Object> getHomeworkStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总作业数
        stats.put("totalHomeworks", count());
        
        // 获取已完成作业数
        stats.put("completedHomeworks", homeworkMapper.getCompletedCount());
        
        // 获取进行中的作业数
        stats.put("ongoingHomeworks", homeworkMapper.getOngoingCount());
        
        // 获取平均完成率
        stats.put("avgCompletionRate", homeworkMapper.getAvgCompletionRate());
        
        return stats;
    }

    @Override
    public Map<String, Long> getCompletionRateDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        // 获取不同完成率范围的作业数量
        List<Map<String, Object>> rawData = homeworkMapper.selectSubmissionRateDistribution();
        
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public Map<String, Long> getDifficultyDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        // 获取不同难度的作业数量
        List<Map<String, Object>> rawData = homeworkMapper.selectDifficultyDistribution();
        
        for (Map<String, Object> data : rawData) {
            String difficulty = (String) data.get("difficulty");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(difficulty, count);
        }
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getWeeklySubmissionTrend() {
        // 获取最近一周的每日提交数量
        return homeworkMapper.selectWeeklySubmissionTrend();
    }

    @Override
    public List<Map<String, Object>> getCourseHomeworkStats() {
        // 获取各课程的作业统计信息
        return homeworkMapper.selectCourseHomeworkStats();
    }

    @Override
    public Map<String, Object> getSubmissionRateDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = homeworkMapper.selectSubmissionRateDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }

    @Override
    public Map<String, Object> getAvgCompletionTimeDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        List<Map<String, Object>> rawData = homeworkMapper.selectAvgCompletionTimeDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            Long count = ((Number) data.get("count")).longValue();
            distribution.put(range, count);
        }
        
        return distribution;
    }
}
