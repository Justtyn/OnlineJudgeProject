package com.example.onlinejudge.service.impl;

import com.example.onlinejudge.entity.Announcement;
import com.example.onlinejudge.mapper.AnnouncementMapper;
import com.example.onlinejudge.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Override
    public Announcement getByTitle(String title) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Announcement::getTitle, title);  // 使用like查询，更灵活
        List<Announcement> list = this.list(queryWrapper);
        
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
    
    @Override
    public Announcement getByContent(String content) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Announcement::getContent, content);  // 使用like查询，更灵活
        List<Announcement> list = this.list(queryWrapper);
        
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
