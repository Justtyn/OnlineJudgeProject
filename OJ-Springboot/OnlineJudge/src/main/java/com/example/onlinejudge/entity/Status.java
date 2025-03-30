package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oj_status")
public class Status {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer problemId;
    private Integer userId;
    private String username;
    private String time;
    private String creatTime;
    private String language;
    private String memory;
    private String status;
}
