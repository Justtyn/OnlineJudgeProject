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

/**
 * 学生服务类，提供用户登录、注册、增删改查等功能
 */
@Service
public class StudentService {
    private static final String PASS_SALT = "OnlineJudge";  // 密码加盐值，防止明文存储被破解

    @Resource
    private StudentMapper studentMapper;  // 依赖注入 Mapper 层，操作数据库

    /**
     * 对密码进行加密处理
     * @param password 用户输入的原始密码
     * @return 加密后的密码
     */
    private String securePass(String password) {
        return SecureUtil.md5(password + PASS_SALT);
    }

    /**
     * 用户登录
     * @param account 传入包含用户名和密码的账号对象
     * @return 返回包含 token 的账号信息
     */
    @Transactional
    public Account login(Account account) {
        // 根据用户名查询数据库中的用户信息
        Account dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (dbStudent == null) {
            throw new CustomException("账号或密码错误");
        }
        // 校验密码，进行加密后比对
        if (!securePass(account.getPassword()).equals(dbStudent.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 生成 token 并返回
        String token = JwtUtil.generateToken(dbStudent.getUsername());
        dbStudent.setToken(token);
        return dbStudent;
    }

    /**
     * 用户注册
     * @param account 传入账号信息
     */
    @Transactional
    public void register(Account account) {
        // 创建一个新的学生对象，并填充用户信息
        Student student = new Student();
        student.setUsername(account.getUsername());
        student.setPassword(account.getPassword());
        student.setSex(account.getSex());
        student.setEmail(account.getEmail());
        student.setPhone(account.getPhone());
        // 调用新增方法，将学生信息插入数据库
        this.add(student);
    }

    /**
     * 新增学生信息
     * @param student 传入学生对象
     */
    public void add(Student student) {
        // 先检查是否已有相同用户名的账号
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (dbStudent != null) {
            throw new CustomException("账号已存在");
        }

        // 如果未提供姓名，则默认使用用户名作为姓名
        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        // 对密码进行加密存储
        student.setPassword(SecureUtil.md5(student.getPassword() + PASS_SALT));
        // 设置用户角色为学生
        student.setRole(RoleEnum.STUDENT.name());

        // 插入数据库
        studentMapper.insert(student);
    }

    /**
     * 根据 ID 查询学生信息
     * @param id 学生 ID
     * @return 返回查询到的学生对象
     */
    public Student getStudentById(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }
        return student;
    }

    /**
     * 查询所有学生信息
     * @return 返回学生列表
     */
    public List<Student> getAllStudents() {
        return studentMapper.selectAll();
    }

    /**
     * 更新学生信息
     * @param student 传入修改后的学生对象
     * @return 是否更新成功
     */
    @Transactional
    public boolean update(Student student) {
        // 先检查学生是否存在
        Student dbStudent = studentMapper.selectById(student.getId());
        if (dbStudent == null) {
            throw new CustomException("学生不存在");
        }

        // 如果更新了用户名，则需要检查新用户名是否已经存在
        if (!dbStudent.getUsername().equals(student.getUsername())) {
            Student existStudent = studentMapper.selectByUsername(student.getUsername());
            if (existStudent != null) {
                throw new CustomException("用户名已存在");
            }
        }

        return studentMapper.update(student) > 0;
    }

    /**
     * 更新学生背景信息
     * @param id 学生 ID
     * @param background 新的背景信息
     * @return 是否更新成功
     */
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
     * 删除学生信息
     * @param id 学生 ID
     * @return 是否删除成功
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

    /**
     * 处理头像上传
     * @param file 上传的头像文件
     * @param id 学生 ID
     * @return 上传后的头像 URL
     * @throws IOException 可能抛出的文件处理异常
     */
    @Transactional
    public String uploadAvatar(MultipartFile file, Integer id) throws IOException {
        // 检查学生是否存在
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new CustomException("学生不存在");
        }

        // 定义上传目录，使用当前工作目录下的 uploads 文件夹
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 若目录不存在，则创建
        }

        // 生成唯一文件名，防止文件名冲突
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        
        // 保存上传的文件到指定路径
        file.transferTo(dest);

        // 生成头像 URL，假设服务器能通过 http://localhost:9090/uploads/ 访问
        String avatarUrl = "http://localhost:9090/uploads/" + fileName;

        // 更新学生的头像信息
        student.setAvatar(avatarUrl);
        int updated = studentMapper.update(student);
        if (updated > 0) {
            return avatarUrl;
        } else {
            throw new CustomException("更新头像失败");
        }
    }
}
