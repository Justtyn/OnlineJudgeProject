// DiscussCommentController.java
package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.DiscussComment;
import com.example.onlinejudge.service.DiscussCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/discuss/comment")
public class DiscussCommentController {

    @Resource
    private DiscussCommentService commentService;

    /**
     * 列表：获取某讨论的评论树
     */
    @GetMapping("/list")
    public Result list(@RequestParam Integer discussId) {
        List<DiscussComment> tree = commentService.getCommentTree(discussId);
        return Result.success(tree);
    }

    /**
     * 新增评论
     */
    @PostMapping("/save")
    public Result save(@RequestBody DiscussComment comment) {
        commentService.add(comment);
        return Result.success("评论添加成功");
    }

    /**
     * 修改评论
     */
    @PutMapping("/update")
    public Result update(@RequestBody DiscussComment comment) {
        commentService.update(comment);
        return Result.success("评论修改成功");
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        commentService.deleteById(id);
        return Result.success("评论删除成功");
    }
}
