package com.example.onlinejudge.service;

import com.example.onlinejudge.entity.Account;
import com.example.onlinejudge.entity.Teacher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TeacherService {
    void add(Teacher teacher);

    Teacher getTeacherById(Integer id);

    List<Teacher> getAllTeachers();

    boolean update(Teacher teacher);

    boolean delete(Integer id);

    Account login(Account account);

    void register(Account account);

    String uploadAvatar(MultipartFile file, Integer id) throws IOException;

    /**
     * 分页获取教师列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页显示数量
     * @return 包含分页信息和教师列表的Map
     */
    Map<String, Object> getTeachersWithPage(Integer pageNum, Integer pageSize);

    /**
     * 根据用户名模糊查询教师信息
     *
     * @param username 用户名关键字
     * @return 匹配的教师列表
     */
    List<Teacher> getTeachersByUsernameLike(String username);

    /**
     * 根据姓名模糊查询教师信息
     *
     * @param name 姓名关键字
     * @return 匹配的教师列表
     */
    List<Teacher> getTeachersByNameLike(String name);

    /**
     * 根据创建时间的年份查询教师信息
     *
     * @param year 年份，如2023
     * @return 匹配的教师列表
     */
    List<Teacher> getTeachersByCreateTimeYear(Integer year);

    /**
     * 根据班级ID查询教师信息
     *
     * @param classId 班级ID
     * @return 匹配的教师列表
     */
    List<Teacher> getTeachersByClassId(Integer classId);

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
     * @param email    邮箱
     * @return 是否成功发送邮件
     */
    boolean resetPassword(String username, String email);

    /**
     * 修改密码
     *
     * @param username    用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功修改密码
     */
    boolean changePassword(String username, String oldPassword, String newPassword);

    // 获取总教师数
    long count();

    // 获取指定时间段内新注册的教师数
    long getNewTeacherCount(LocalDate startDate, LocalDate endDate);
}

