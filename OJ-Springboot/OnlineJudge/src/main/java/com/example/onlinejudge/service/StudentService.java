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
}
