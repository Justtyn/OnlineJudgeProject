package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("score")
public class Course {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private int creatorId;
    private int homeworkQuantity;
}
