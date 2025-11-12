package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@TableName("oj_status")
@ApiModel(description = "提交状态实体")
public class Status {
    @ApiModelProperty("状态ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("问题ID")
    private Integer problemId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("执行时间")
    private String time;

    @ApiModelProperty("创建时间")
    private String creatTime;

    @ApiModelProperty("编程语言")
    private String language;

    @ApiModelProperty("内存使用")
    private String memory;

    @ApiModelProperty("提交状态")
    private String status;

    @ApiModelProperty("提交代码")
    private String code;

    @ApiModelProperty("输出结果")
    private String output;
}
