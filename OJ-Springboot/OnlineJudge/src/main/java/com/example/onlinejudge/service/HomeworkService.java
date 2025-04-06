package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Homework;

import java.util.List;

public interface HomeworkService {
    // 添加作业
    boolean addHomework(Homework homework);
    
    // 删除作业
    boolean deleteHomework(Integer id);
    
    // 分页查询所有作业
    Page<Homework> listHomeworkByPage(Integer current, Integer size);
    
    // 按classId查询作业
    List<Homework> listHomeworkByClassId(Integer classId);
}
