package com.example.onlinejudge.service;

import com.example.onlinejudge.entity.AccessLog;
import com.example.onlinejudge.mapper.AccessLogMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AccessLogWriter {
    private final BlockingQueue<AccessLog> queue = new LinkedBlockingQueue<>(10000);

    @Resource
    private AccessLogMapper accessLogMapper;

    public void offer(AccessLog log) {
        // 丢日志可接受：队列满则直接丢弃
        queue.offer(log);
    }

    @PostConstruct
    public void init() {
    }

    @Scheduled(fixedDelay = 2000)
    public void flush() {
        List<AccessLog> batch = new ArrayList<>(512);
        queue.drainTo(batch, 512);
        if (batch.isEmpty()) return;
        try {
            accessLogMapper.batchInsert(batch);
        } catch (Exception e) {
            // 降级：逐条插入，避免整批失败全部丢失
            for (AccessLog log : batch) {
                try {
                    accessLogMapper.insert(log);
                } catch (Exception ignore) {
                }
            }
        }
    }
}


