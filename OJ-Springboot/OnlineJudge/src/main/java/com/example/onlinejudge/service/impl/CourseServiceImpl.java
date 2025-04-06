package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Course;
import com.example.onlinejudge.mapper.CourseMapper;
import com.example.onlinejudge.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    
    @Override
    public boolean addCourse(Course course) {
        return save(course);
    }
    
    @Override
    public boolean deleteCourse(Integer id) {
        return removeById(id);
    }
    
    @Override
    public Page<Course> listCourses(Integer current, Integer size) {
        Page<Course> page = new Page<>(current, size);
        return page(page);
    }
    
    @Override
    public Course getCourseById(Integer id) {
        return getById(id);
    }
}
