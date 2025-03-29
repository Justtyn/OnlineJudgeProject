package com.example.onlinejudge.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.onlinejudge.entity.Problem;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.ProblemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemService {
    
    @Resource
    private ProblemMapper problemMapper;
    
    /**
     * 分页查询所有问题
     */
    public Map<String, Object> selectPage(Integer pageNum, Integer pageSize) {
        // 计算分页起始位置
        pageNum = (pageNum - 1) * pageSize;
        List<Problem> problemList = problemMapper.selectPage(pageNum, pageSize);
        Long total = problemMapper.selectTotal();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", problemList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    /**
     * 根据ID查询问题
     */
    public Problem selectById(Integer id) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new CustomException("问题不存在");
        }
        return problem;
    }
    
    /**
     * 根据名称查询问题
     */
    public Map<String, Object> selectByName(String name, Integer pageNum, Integer pageSize) {
        pageNum = (pageNum - 1) * pageSize;
        List<Problem> problemList = problemMapper.selectByName(name, pageNum, pageSize);
        Long total = problemMapper.selectTotalByName(name);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", problemList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    /**
     * 根据出题人查询问题
     */
    public Map<String, Object> selectBySetter(String setter, Integer pageNum, Integer pageSize) {
        pageNum = (pageNum - 1) * pageSize;
        List<Problem> problemList = problemMapper.selectBySetter(setter, pageNum, pageSize);
        Long total = problemMapper.selectTotalBySetter(setter);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", problemList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    /**
     * 添加问题
     */
    @Transactional
    public void add(Problem problem) {
        if (ObjectUtil.isEmpty(problem.getName())) {
            throw new CustomException("问题名称不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getSetter())) {
            throw new CustomException("出题人不能为空");
        }
        
        // 设置默认值
        if (problem.getAcCount() == null) {
            problem.setAcCount(0);
        }
        if (problem.getSubmitCount() == null) {
            problem.setSubmitCount(0);
        }
        
        // 设置创建时间
        problem.setCreateTime(DateUtil.now());
        
        problemMapper.insert(problem);
    }
    
    /**
     * 修改问题
     */
    @Transactional
    public void update(Problem problem) {
        if (problem.getId() == null) {
            throw new CustomException("问题ID不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getName())) {
            throw new CustomException("问题名称不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getSetter())) {
            throw new CustomException("出题人不能为空");
        }
        
        // 查询原问题信息
        Problem dbProblem = problemMapper.selectById(problem.getId());
        if (dbProblem == null) {
            throw new CustomException("问题不存在");
        }
        
        problemMapper.update(problem);
    }
    
    /**
     * 删除问题
     */
    @Transactional
    public void deleteById(Integer id) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new CustomException("问题不存在");
        }
        
        problemMapper.deleteById(id);
    }
} 