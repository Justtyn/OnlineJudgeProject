// DiscussEntity.java
package com.example.onlinejudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("discuss")
public class Discuss {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer problemId;
    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String content;
    private Integer viewNum;

    /**
     * 详情接口时返回的评论树
     */
    @TableField(exist = false)
    private List<DiscussComment> comments;
}
