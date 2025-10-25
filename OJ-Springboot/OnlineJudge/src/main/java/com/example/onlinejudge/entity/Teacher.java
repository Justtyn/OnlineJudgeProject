package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("teacher")
@ApiModel(description = "教师实体类")
public class Teacher {
    @ApiModelProperty("教师ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("密码")
    private String password;
    
    @ApiModelProperty("姓名")
    private String name;
    
    @ApiModelProperty("性别")
    private String sex;
    
    @ApiModelProperty("电话")
    private String phone;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("头像")
    private String avatar;
    
    @ApiModelProperty("角色")
    private String role;
    
    @ApiModelProperty("所属班级ID")
    private Integer classId;
}
