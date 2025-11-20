package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Course;
import com.example.onlinejudge.mapper.ScoreMapper;
import com.example.onlinejudge.service.ScoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Course> implements ScoreService {

    @Resource
    private ScoreMapper scoreMapper;

    @Override
    public long count() {
        return scoreMapper.selectCount(null);
    }

    @Override
    public Map<String, Long> getHomeworkCountDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        List<Map<String, Object>> rawData = scoreMapper.selectHomeworkCountDistribution();
        for (Map<String, Object> data : rawData) {
            String range = (String) data.get("range");
            if (range == null) {
                range = (String) data.get("homework_qty_range");
            }
            Number countValue = (Number) data.get("count");
            if (countValue == null) {
                countValue = (Number) data.get("cnt");
            }
            if (range != null && countValue != null) {
                distribution.put(range, countValue.longValue());
            }
        }
        return distribution;
    }
}
