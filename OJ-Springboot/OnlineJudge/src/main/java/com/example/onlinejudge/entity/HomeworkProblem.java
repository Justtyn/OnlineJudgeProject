package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

@Data
@TableName("homework_problem")
@ApiModel(description = "作业题目关联实体")
public class HomeworkProblem {
    @ApiModelProperty("作业ID")
    private int homeworkId;

    @ApiModelProperty("题目ID")
    private int problemId;

    @ApiModelProperty("通过次数")
    private int acCount;

    @ApiModelProperty("提交次数")
    private int submitCount;
}
