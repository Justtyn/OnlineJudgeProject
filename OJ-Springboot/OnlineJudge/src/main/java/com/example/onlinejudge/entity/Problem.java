package com.example.onlinejudge.entity;

import lombok.Data;

@Data
public class Problem {
    private Integer id;             // 题号
    private String name;            // 问题名称
    private String setter;          // 出题人
    private String createTime;      // 出题时间
    private Integer acCount;        // 通过数量
    private Integer submitCount;    // 提交数量
    private String desc;            // 题目描述
    private String descInput;       // 输入描述
    private String descOutput;      // 输出描述
    private String sampleInput;     // 输入样例
    private String sampleOutput;    // 输出样例
    private String hint;            // 提示说明
} 