package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Solution;
import com.example.onlinejudge.mapper.SolutionMapper;
import com.example.onlinejudge.service.SolutionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution> implements SolutionService {

    @Override
    public boolean addSolution(Solution solution) {
        solution.setCreateTime(LocalDateTime.now());
        solution.setLikeNum(0);
        return save(solution);
    }

    @Override
    public boolean deleteSolution(Integer id) {
        return removeById(id);
    }

    @Override
    public Page<Solution> getSolutionPage(Integer current, Integer size) {
        Page<Solution> page = new Page<>(current, size);
        return page(page);
    }

    @Override
    public Solution getSolutionById(Integer id) {
        return getById(id);
    }

    @Override
    public boolean increaseLike(Integer id) {
        return baseMapper.increaseLike(id) > 0;
    }
}
