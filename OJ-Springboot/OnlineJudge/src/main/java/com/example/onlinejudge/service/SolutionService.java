package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Solution;

public interface SolutionService extends IService<Solution> {
    
    /**
     * 添加题解
     * @param solution 题解信息
     * @return 是否添加成功
     */
    boolean addSolution(Solution solution);
    
    /**
     * 删除题解
     * @param id 题解ID
     * @return 是否删除成功
     */
    boolean deleteSolution(Integer id);
    
    /**
     * 分页查询题解
     * @param current 当前页
     * @param size 每页大小
     * @return 题解分页数据
     */
    Page<Solution> getSolutionPage(Integer current, Integer size);
    
    /**
     * 根据ID查询题解
     * @param id 题解ID
     * @return 题解信息
     */
    Solution getSolutionById(Integer id);
    
    /**
     * 增加题解点赞数
     * @param id 题解ID
     * @return 是否点赞成功
     */
    boolean increaseLike(Integer id);
}
