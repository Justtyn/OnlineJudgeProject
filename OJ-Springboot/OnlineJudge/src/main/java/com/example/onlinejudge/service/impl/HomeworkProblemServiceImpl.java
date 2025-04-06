package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.HomeworkProblem;
import com.example.onlinejudge.mapper.HomeworkProblemMapper;
import com.example.onlinejudge.service.HomeworkProblemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeworkProblemServiceImpl extends ServiceImpl<HomeworkProblemMapper, HomeworkProblem> implements HomeworkProblemService {
    
    @Override
    public boolean addHomeworkProblem(HomeworkProblem homeworkProblem) {
        return save(homeworkProblem);
    }
    
    @Override
    public boolean removeHomeworkProblem(int homeworkId, int problemId) {
        LambdaQueryWrapper<HomeworkProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeworkProblem::getHomeworkId, homeworkId)
                   .eq(HomeworkProblem::getProblemId, problemId);
        return remove(queryWrapper);
    }
    
    @Override
    public List<HomeworkProblem> getHomeworkProblemsByHomeworkId(int homeworkId) {
        LambdaQueryWrapper<HomeworkProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeworkProblem::getHomeworkId, homeworkId);
        return list(queryWrapper);
    }
}
