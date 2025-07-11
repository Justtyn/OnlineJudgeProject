package com.example.onlinejudge.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinejudge.entity.Announcement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public interface AnnouncementService extends IService<Announcement>  {
    Announcement getByTitle(String title);
    Announcement getByContent(String content);

    Map<String, Object> getRecentAnnouncementsStats();

    Map<String, Object> getTimeDistribution();

    Map<String, Object> getTypeDistribution();
}
