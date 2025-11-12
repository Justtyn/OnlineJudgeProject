// DiscussEntity.java
package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("discuss")
@ApiModel(description = "讨论实体")
public class Discuss {
    @ApiModelProperty("讨论ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("问题ID")
    private Integer problemId;

    @ApiModelProperty("讨论标题")
    private String title;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("讨论内容")
    private String content;

    @ApiModelProperty("浏览次数")
    private Integer viewNum;

    /**
     * 详情接口时返回的评论树
     */
    @ApiModelProperty("评论列表")
    @TableField(exist = false)
    private List<DiscussComment> comments;
}
