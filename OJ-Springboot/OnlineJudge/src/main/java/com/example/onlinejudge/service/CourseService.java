package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.onlinejudge.entity.Course;
import java.util.List;

public interface CourseService {
    
    /**
     * 添加课程
     * @param course 课程信息
     * @return 添加结果
     */
    boolean addCourse(Course course);
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 删除结果
     */
    boolean deleteCourse(Integer id);
    
    /**
     * 分页查询所有课程
     * @param current 当前页
     * @param size 每页大小
     * @return 课程分页结果
     */
    Page<Course> listCourses(Integer current, Integer size);
    
    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程信息
     */
    Course getCourseById(Integer id);

    /**
     * 获取所有课程列表（不分页）
     * @return 所有课程列表
     */
    List<Course> listAllCourses();
}
