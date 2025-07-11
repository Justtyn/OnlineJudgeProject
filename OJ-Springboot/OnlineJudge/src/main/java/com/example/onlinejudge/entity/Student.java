package com.example.onlinejudge.entity;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "学生实体类")
public class Student extends Admin {
    @ApiModelProperty("学生背景介绍")
    private String background;
    
    @ApiModelProperty("通过的题目数量")
    private int ac;
    
    @ApiModelProperty("提交的题目总数")
    private int submit;
    
    @ApiModelProperty("所属学校")
    private String school;
    
    @ApiModelProperty("总分数")
    private int score;
    
    @ApiModelProperty("最后登录时间")
    private String lastLoginTime;
    
    @ApiModelProperty("最后使用的编程语言")
    private String lastLanguage;
    
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @ApiModelProperty("最后访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitTime;
    
    @ApiModelProperty("班级ID")
    private int classId;
    
    @ApiModelProperty("每日挑战记录")
    private String dailyChallenge;
}
