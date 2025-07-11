package com.example.onlinejudge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Course;
import java.util.List;
import java.util.Map;

public interface CourseService extends IService<Course> {
    
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

    // 获取课程总数
    long count();

    /** 课程学生数区间分布 */
    Map<String, Long> getStudentCountDistribution();

    /** 课程作业数区间分布 */
    Map<String, Long> getHomeworkCountDistribution();

    /** 最活跃课程 */
    List<Map<String, Object>> getMostActiveCourses(int limit);
}
