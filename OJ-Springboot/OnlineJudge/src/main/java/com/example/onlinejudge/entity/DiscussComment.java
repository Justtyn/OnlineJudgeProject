// DiscussCommentEntity.java
package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("discuss_comment")
public class DiscussComment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer discussId;
    private Integer userId;
    private Integer parentCommentId;
    private Integer replyToUserId;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 用于树形结构拼装
     */
    @TableField(exist = false)
    private List<DiscussComment> children;
}
