package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("homework_problem")
public class HomeworkProblem {
    private int homeworkId;
    private int problemId;
    private int acCount;
    private int submitCount;
}
