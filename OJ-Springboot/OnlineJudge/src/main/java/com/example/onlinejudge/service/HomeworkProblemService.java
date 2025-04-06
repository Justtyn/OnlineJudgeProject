package com.example.onlinejudge.service;

import com.example.onlinejudge.entity.HomeworkProblem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HomeworkProblemService extends IService<HomeworkProblem> {
    
    /**
     * 添加作业题目关联
     * @param homeworkProblem 作业题目关联对象
     * @return 是否添加成功
     */
    boolean addHomeworkProblem(HomeworkProblem homeworkProblem);
    
    /**
     * 删除作业题目关联
     * @param homeworkId 作业ID
     * @param problemId 题目ID
     * @return 是否删除成功
     */
    boolean removeHomeworkProblem(int homeworkId, int problemId);
    
    /**
     * 根据作业ID查询所有关联的题目
     * @param homeworkId 作业ID
     * @return 题目列表
     */
    List<HomeworkProblem> getHomeworkProblemsByHomeworkId(int homeworkId);
    
    /**
     * 增加作业题目的提交次数
     * @param homeworkId 作业ID
     * @param problemId 题目ID
     * @return 是否增加成功
     */
    boolean incrementSubmitCount(int homeworkId, int problemId);
    
    /**
     * 增加作业题目的通过次数
     * @param homeworkId 作业ID
     * @param problemId 题目ID
     * @return 是否增加成功
     */
    boolean incrementAcCount(int homeworkId, int problemId);
}
