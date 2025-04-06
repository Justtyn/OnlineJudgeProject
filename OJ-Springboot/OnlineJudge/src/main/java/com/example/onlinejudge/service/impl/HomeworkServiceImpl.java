package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Homework;
import com.example.onlinejudge.mapper.HomeworkMapper;
import com.example.onlinejudge.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeworkServiceImpl implements HomeworkService {
    
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
}
