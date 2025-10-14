package com.example.onlinejudge.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "教师实体类")
public class Teacher extends Admin {
    @ApiModelProperty("教师ID")
    private Integer id;
    
    @ApiModelProperty("所属班级ID")
    private Integer classId;
    
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @ApiModelProperty("最后登录时间")
    private String lastLoginTime;
    
    @ApiModelProperty("最后使用的编程语言")
    private String lastLanguage;
    
    @ApiModelProperty("最后访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitTime;
}
