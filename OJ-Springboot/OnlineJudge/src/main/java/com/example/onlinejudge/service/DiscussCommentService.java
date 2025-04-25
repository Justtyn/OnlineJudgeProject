// DiscussCommentService.java
package com.example.onlinejudge.service;

import com.example.onlinejudge.entity.DiscussComment;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.DiscussCommentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscussCommentService {

    @Resource
    private DiscussCommentMapper commentMapper;

    /**
     * 查询并组装某讨论的评论树
     */
    public List<DiscussComment> getCommentTree(Integer discussId) {
        List<DiscussComment> all = commentMapper.selectByDiscussId(discussId);
        // 映射 id → comment
        Map<Integer, DiscussComment> map = all.stream()
                .collect(Collectors.toMap(DiscussComment::getId, c -> c));
        List<DiscussComment> tree = new ArrayList<>();
        for (DiscussComment c : all) {
            if (c.getParentCommentId() == null) {
                tree.add(c);
            } else {
                DiscussComment p = map.get(c.getParentCommentId());
                if (p != null) {
                    if (p.getChildren() == null) {
                        p.setChildren(new ArrayList<>());
                    }
                    p.getChildren().add(c);
                }
            }
        }
        return tree;
    }

    /** 新增评论 */
    public void add(DiscussComment comment) {
        if (comment.getDiscussId() == null || comment.getUserId() == null) {
            throw new CustomException("讨论ID和用户ID不能为空");
        }
        commentMapper.insert(comment);
    }

    /** 修改评论 */
    public void update(DiscussComment comment) {
        if (comment.getId() == null) {
            throw new CustomException("评论ID不能为空");
        }
        commentMapper.update(comment);
    }

    /** 删除评论 */
    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }
}
