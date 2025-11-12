// DiscussCommentEntity.java
package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("discuss_comment")
@ApiModel(description = "讨论评论实体")
public class DiscussComment {
    @ApiModelProperty("评论ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("讨论ID")
    private Integer discussId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("父评论ID")
    private Integer parentCommentId;

    @ApiModelProperty("回复用户ID")
    private Integer replyToUserId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 用于树形结构拼装
     */
    @ApiModelProperty("子评论列表")
    @TableField(exist = false)
    private List<DiscussComment> children;
}
