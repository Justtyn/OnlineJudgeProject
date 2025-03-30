package com.example.onlinejudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinejudge.entity.Status;
import com.example.onlinejudge.mapper.StatusMapper;
import com.example.onlinejudge.service.StatusService;
import org.springframework.stereotype.Service;

@Service
public class StatusServiceImpl extends ServiceImpl<StatusMapper, Status> implements StatusService {
} 