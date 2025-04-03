package com.example.onlinejudge.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Announcement;

public interface AnnouncementService extends IService<Announcement>  {
    Announcement getByTitle(String title);
    Announcement getByContent(String content);
}
