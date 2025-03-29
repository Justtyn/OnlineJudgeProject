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

@Service
public class StudentService {
    private static final String PASS_SALT = "OnlineJudge";
    @Resource
    private StudentMapper studentMapper;

    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    /**
     * 登陆
     */
    @Transactional
    public Account login(Account account) {
        Account dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (dbStudent == null) {
            // 数据库没有用户
            throw new CustomException("账号或密码错误");
        }
        // 比较密码
        if (!securePass(account.getPassword()).equals(dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 登陆成功
        // 生成 token
        String token = JwtUtil.generateToken(dbStudent.getUsername());
        dbStudent.setToken(token);
        return dbStudent;
    }

    // 注册方法
    @Transactional
    public void register(Account account) {
        Student student = new Student();
        student.setUsername(account.getUsername());
        student.setPassword(account.getPassword());
        student.setSex(account.getSex());
        student.setEmail(account.getEmail());
        student.setPhone(account.getPhone());
        this.add(student);
    }

    // 新增方法
    public void add(Student student) {
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (dbStudent != null) {    // 已有同名账号，不允许插入
            throw new CustomException("账号已存在");
        }

        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setPassword(SecureUtil.md5(student.getPassword() + PASS_SALT));
        student.setRole(RoleEnum.STUDENT.name());

        studentMapper.insert(student);
    }
    
    /**
     * 根据ID查询学生
     */
    public Student getStudentById(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return student;
    }
    
    /**
     * 获取所有学生
     */
    public List<Student> getAllStudents() {
        return studentMapper.selectAll();
    }
    
    /**
     * 更新学生信息
     */
    @Transactional
    public boolean update(Student student) {
        // 检查学生是否存在
        Student dbStudent = studentMapper.selectById(student.getId());
        if (dbStudent == null) {
            throw new CustomException("学生不存在");
        }
        
        // 如果更新了用户名，检查新用户名是否已存在
        if (!dbStudent.getUsername().equals(student.getUsername())) {
            Student existStudent = studentMapper.selectByUsername(student.getUsername());
            if (existStudent != null) {
                throw new CustomException("用户名已存在");
            }
        }
        
        return studentMapper.update(student) > 0;
    }

    @Transactional
    public boolean updateBackground(Integer id, String background) {

        // 检查学生是否存在
        Student dbStudent = studentMapper.selectById(id);
        if (dbStudent == null) {
            throw new CustomException("学生不存在");
        }

        int rows = studentMapper.updateBackground(id, background);
        return rows > 0;
    }


    /**
     * 删除学生
     */
    @Transactional
    public boolean delete(Integer id) {
        // 检查学生是否存在
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        
        return studentMapper.deleteById(id) > 0;
    }

    @Transactional
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        // 检查学生是否存在
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        // 使用绝对路径作为上传目录（例如：当前工作目录下的 uploads 文件夹）
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建上传目录
        }
        // 生成唯一文件名
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        // 将上传的文件保存到目标路径
        file.transferTo(dest);
        // 生成头像 URL，假设服务器能通过 http://localhost:9090/uploads/ 访问上传的文件
        String avatarUrl = "http://localhost:9090/uploads/" + fileName;
        // 更新学生头像信息
        student.setAvatar(avatarUrl);
        int updated = studentMapper.update(student);
        if (updated > 0) {
            return avatarUrl;
        } else {
            throw new CustomException("更新头像失败");
        }
    }


}
