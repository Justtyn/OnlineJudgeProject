package com.example.onlinejudge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Announcement;
import com.example.onlinejudge.service.AnnouncementService;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // 添加状态
    @PostMapping
    public Result<?> save(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    // 删除状态
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        announcementService.removeById(id);
        return Result.success();
    }

    // 不分页查询全部状态
    @GetMapping
    public Result<?> findAll() {
        return Result.success(announcementService.list());
    }

    // 根据标题title查询
    @GetMapping("/title")
    public Result<?> findByTitle(@RequestParam String title) {
        Announcement announcement = announcementService.getByTitle(title);
        return Result.success(announcement);
    }

    // 根据内容content查询
    @GetMapping("/content")
    public Result<?> findByContent(@RequestParam String content) {
        Announcement announcement = announcementService.getByContent(content);
        return Result.success(announcement);
    }
}
