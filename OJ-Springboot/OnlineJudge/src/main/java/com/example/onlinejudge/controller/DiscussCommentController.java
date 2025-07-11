// DiscussCommentController.java
package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.DiscussComment;
import com.example.onlinejudge.service.DiscussCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "讨论评论管理接口")
@RestController
@RequestMapping("/discuss/comment")
public class DiscussCommentController {

    @Resource
    private DiscussCommentService commentService;

    @ApiOperation("获取讨论评论树")
    @GetMapping("/list")
    public Result list(@ApiParam("讨论ID") @RequestParam Integer discussId) {
        List<DiscussComment> tree = commentService.getCommentTree(discussId);
        return Result.success(tree);
    }

    @ApiOperation("新增评论")
    @PostMapping("/save")
    public Result save(@ApiParam("评论信息") @RequestBody DiscussComment comment) {
        commentService.add(comment);
        return Result.success("评论添加成功");
    }

    @ApiOperation("修改评论")
    @PutMapping("/update")
    public Result update(@ApiParam("评论信息") @RequestBody DiscussComment comment) {
        commentService.update(comment);
        return Result.success("评论修改成功");
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/delete/{id}")
    public Result delete(@ApiParam("评论ID") @PathVariable Integer id) {
        commentService.deleteById(id);
        return Result.success("评论删除成功");
    }
}
