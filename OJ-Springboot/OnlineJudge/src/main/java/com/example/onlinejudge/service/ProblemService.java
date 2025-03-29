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

/**
 * 问题管理服务类
 * 提供对编程题目的增删改查等基本操作
 */
@Service
public class ProblemService {
    
    /**
     * 注入问题数据访问层接口
     */
    @Resource
    private ProblemMapper problemMapper;
    
    /**
     * 分页查询所有问题
     * @param pageNum 当前页码
     * @param pageSize 每页显示条数
     * @return 返回包含问题列表和总数的Map
     *         - list: 当前页的问题列表
     *         - total: 问题总数
     */
    public Map<String, Object> selectPage(Integer pageNum, Integer pageSize) {
        // 计算分页起始位置：(页码-1) * 每页条数
        pageNum = (pageNum - 1) * pageSize;
        List<Problem> problemList = problemMapper.selectPage(pageNum, pageSize);
        Long total = problemMapper.selectTotal();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", problemList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    /**
     * 根据ID查询单个问题
     * @param id 问题ID
     * @return 问题实体
     * @throws CustomException 当问题不存在时抛出异常
     */
    public Problem selectById(Integer id) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new CustomException("问题不存在");
        }
        return problem;
    }
    
    /**
     * 根据问题名称模糊查询并分页
     * @param name 问题名称关键字
     * @param pageNum 当前页码
     * @param pageSize 每页显示条数
     * @return 返回包含问题列表和总数的Map
     *         - list: 当前页的问题列表
     *         - total: 符合条件的问题总数
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
     * 根据出题人查询并分页
     * @param setter 出题人
     * @param pageNum 当前页码
     * @param pageSize 每页显示条数
     * @return 返回包含问题列表和总数的Map
     *         - list: 当前页的问题列表
     *         - total: 符合条件的问题总数
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
     * 添加新问题
     * @param problem 问题实体
     * @throws CustomException 当问题名称或出题人为空时抛出异常
     */
    @Transactional
    public void add(Problem problem) {
        // 验证必填字段
        if (ObjectUtil.isEmpty(problem.getName())) {
            throw new CustomException("问题名称不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getSetter())) {
            throw new CustomException("出题人不能为空");
        }
        
        // 设置默认值：通过次数和提交次数初始化为0
        if (problem.getAcCount() == null) {
            problem.setAcCount(0);
        }
        if (problem.getSubmitCount() == null) {
            problem.setSubmitCount(0);
        }
        
        // 设置创建时间为当前时间
        problem.setCreateTime(DateUtil.now());
        
        problemMapper.insert(problem);
    }
    
    /**
     * 更新问题信息
     * @param problem 更新后的问题实体
     * @throws CustomException 当问题ID不存在、问题名称或出题人为空时抛出异常
     */
    @Transactional
    public void update(Problem problem) {
        // 验证必填字段
        if (problem.getId() == null) {
            throw new CustomException("问题ID不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getName())) {
            throw new CustomException("问题名称不能为空");
        }
        if (ObjectUtil.isEmpty(problem.getSetter())) {
            throw new CustomException("出题人不能为空");
        }
        
        // 确认问题是否存在
        Problem dbProblem = problemMapper.selectById(problem.getId());
        if (dbProblem == null) {
            throw new CustomException("问题不存在");
        }
        
        problemMapper.update(problem);
    }
    
    /**
     * 根据ID删除问题
     * @param id 问题ID
     * @throws CustomException 当问题不存在时抛出异常
     */
    @Transactional
    public void deleteById(Integer id) {
        // 确认问题是否存在
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new CustomException("问题不存在");
        }
        
        problemMapper.deleteById(id);
    }
} 