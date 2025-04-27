package com.example.onlinejudge.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.common.RoleEnum;
import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Student;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.StudentMapper;
import com.example.onlinejudge.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StudentService {
    void add(Student student);
    Student getStudentById(Integer id);
    List<Student> getAllStudents();
    boolean update(Student student);
    boolean delete(Integer id);
    Account login(Account account);
    void register(Account account);
    String uploadAvatar(MultipartFile file, Integer id) throws IOException;
    boolean updateBackground(Integer id, String background);
    boolean incrementAc(Integer id);
    boolean incrementSubmit(Integer id);
    List<Student> getStudentsOrderByAc();

    /**
     * 分页获取按AC数量降序排列的学生列表
     * 
     * @param pageNum 当前页码
     * @param pageSize 每页显示数量
     * @return 包含分页信息和学生列表的Map
     */
    Map<String, Object> getStudentsOrderByAc(Integer pageNum, Integer pageSize);

    /**
     * 根据用户名模糊查询学生信息
     * 
     * @param username 用户名关键字
     * @return 匹配的学生列表
     */
    List<Student> getStudentsByUsernameLike(String username);

    /**
     * 根据姓名模糊查询学生信息
     * 
     * @param name 姓名关键字
     * @return 匹配的学生列表
     */
    List<Student> getStudentsByNameLike(String name);

    /**
     * 根据创建时间的年份查询学生信息
     * 
     * @param year 年份，如2023
     * @return 匹配的学生列表
     */
    List<Student> getStudentsByCreateTimeYear(Integer year);

    /**
     * 检查用户名是否已存在
     * 
     * @param username 要检查的用户名
     * @return 如果用户名已存在返回true，否则返回false
     */
    boolean isUsernameExists(String username);

    /**
     * 找回密码
     * 
     * @param username 用户名
     * @param email 邮箱
     * @return 是否成功发送邮件
     */
    boolean resetPassword(String username, String email);

    /**
     * 修改密码
     * 
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功修改密码
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
}
