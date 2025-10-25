package com.example.onlinejudge.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Announcement;
import com.example.onlinejudge.service.AnnouncementService;

@Api(tags = "公告管理接口")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @ApiOperation("添加公告")
    @PostMapping
    public Result<?> save(@ApiParam("公告信息") @RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    @ApiOperation("更新公告")
    @PutMapping
    public Result<?> update(@ApiParam("公告信息") @RequestBody Announcement announcement) {
        announcementService.updateById(announcement);
        return Result.success();
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    public Result<?> delete(@ApiParam("公告ID") @PathVariable Integer id) {
        announcementService.removeById(id);
        return Result.success();
    }

    @ApiOperation("查询所有公告")
    @GetMapping
    public Result<?> findAll() {
        return Result.success(announcementService.list());
    }

    @ApiOperation("根据标题查询公告")
    @GetMapping("/title")
    public Result<?> findByTitle(@ApiParam("公告标题") @RequestParam String title) {
        Announcement announcement = announcementService.getByTitle(title);
        return Result.success(announcement);
    }

    @ApiOperation("根据内容查询公告")
    @GetMapping("/content")
    public Result<?> findByContent(@ApiParam("公告内容") @RequestParam String content) {
        Announcement announcement = announcementService.getByContent(content);
        return Result.success(announcement);
    }
}
