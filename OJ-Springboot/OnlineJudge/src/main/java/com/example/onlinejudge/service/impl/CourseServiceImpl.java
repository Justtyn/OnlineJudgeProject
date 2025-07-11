package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Course;
import com.example.onlinejudge.mapper.CourseMapper;
import com.example.onlinejudge.service.CourseService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    
    @Resource
    private CourseMapper courseMapper;

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

    @Override
    public List<Course> listAllCourses() {
        log.debug("开始查询所有课程");
        List<Course> courses = list();
        log.debug("查询到的课程数量: {}", courses.size());
        log.debug("课程列表: {}", courses);
        return courses;
    }

    @Override
    public long count() {
        return courseMapper.selectCount(null);
    }

    @Override
    public Map<String, Long> getStudentCountDistribution() {
        return courseMapper.selectStudentCountDistribution().stream()
            .collect(Collectors.toMap(
                m -> (String) m.get("student_count_range"),
                m -> ((Number) m.get("cnt")).longValue()
            ));
    }

    @Override
    public Map<String, Long> getHomeworkCountDistribution() {
        return courseMapper.selectHomeworkCountDistribution().stream()
            .collect(Collectors.toMap(
                m -> (String) m.get("homework_qty_range"),
                m -> ((Number) m.get("cnt")).longValue()
            ));
    }

    @Override
    public List<Map<String, Object>> getMostActiveCourses(int limit) {
        return courseMapper.selectMostActiveCourses(limit);
    }
}
