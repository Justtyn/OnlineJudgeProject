package com.example.onlinejudge.entity;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "账户基础实体")
public class Account {
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("密码")
    private String password;
    
    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("姓名")
    private String name;
    
    @ApiModelProperty("性别")
    private String sex;
    
    @ApiModelProperty("出生日期")
    private String birth;
    
    @ApiModelProperty("电话号码")
    private String phone;
    
    @ApiModelProperty("电子邮箱")
    private String email;
    
    @ApiModelProperty("头像地址")
    private String avatar;

    @ApiModelProperty("认证令牌")
    private String token;


}
