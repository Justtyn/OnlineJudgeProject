package com.example.onlinejudge.entity;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "题目实体")
public class Problem {
    @ApiModelProperty("题目ID")
    private Integer id;             // 题号
    @ApiModelProperty("题目名称")
    private String name;            // 问题名称
    @ApiModelProperty("出题人")
    private String setter;          // 出题人
    @ApiModelProperty("创建时间")
    private String createTime;      // 出题时间
    @ApiModelProperty("通过次数")
    private Integer acCount;        // 通过数量
    @ApiModelProperty("提交次数")
    private Integer submitCount;    // 提交数量
    @ApiModelProperty("题目描述")
    private String desc;            // 题目描述
    @ApiModelProperty("输入描述")
    private String descInput;       // 输入描述
    @ApiModelProperty("输出描述")
    private String descOutput;      // 输出描述
    @ApiModelProperty("输入样例")
    private String sampleInput;     // 输入样例
    @ApiModelProperty("输出样例")
    private String sampleOutput;    // 输出样例
    @ApiModelProperty("提示说明")
    private String hint;            // 提示说明
    @ApiModelProperty(value = "是否为每日挑战题", notes = "TRUE表示是，FALSE表示否")
    private String dailyChallenge;  // 是否为每日挑战题，"TRUE"表示是，"FALSE"表示否
} 