package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

@Data
@TableName("score")
@ApiModel(description = "课程实体")
public class Course {
    @ApiModelProperty("课程ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("课程名称")
    private String name;

    @ApiModelProperty("创建者ID")
    private int creatorId;

    @ApiModelProperty("作业数量")
    private int homeworkQuantity;
}
