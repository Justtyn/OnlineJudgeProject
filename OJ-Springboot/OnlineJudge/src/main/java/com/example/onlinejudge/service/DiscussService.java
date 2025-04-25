// DiscussService.java
package com.example.onlinejudge.service;

import cn.hutool.core.date.DateUtil;
import com.example.onlinejudge.entity.Discuss;
import com.example.onlinejudge.exception.CustomException;
import com.example.onlinejudge.mapper.DiscussMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DiscussService {

    @Resource
    private DiscussMapper discussMapper;
    @Resource
    private DiscussCommentService commentService;

    /**
     * 分页倒序查询讨论列表
     * @param pageNum 当前页（1 起）
     * @param pageSize 每页数量
     * @return 包含 list 和 total 的 Map
     */
    public Map<String, Object> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Discuss> list = discussMapper.selectPage(offset, pageSize);
        Long total = discussMapper.selectTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", total);
        return map;
    }

    /**
     * 根据 ID 查询讨论详情并组装评论树，同时 +1 浏览量
     * @param id 讨论ID
     */
    public Discuss selectById(Integer id) {
        Discuss d = discussMapper.selectById(id);
        if (d == null) {
            throw new CustomException("讨论不存在");
        }
        // 浏览量自增
        discussMapper.incrementViewNum(id);
        // 取评论树
        d.setComments(commentService.getCommentTree(id));
        return d;
    }

    /**
     * 新增讨论
     * @param discuss 讨论实体
     */
    public void add(Discuss discuss) {
        // 校验
        if (discuss.getUserId() == null) {
            throw new CustomException("发布者ID不能为空");
        }
        if (discuss.getTitle() == null || discuss.getTitle().isEmpty()) {
            throw new CustomException("标题不能为空");
        }
        // 设置时间、浏览量初始
        discuss.setCreateTime(DateUtil.date().toLocalDateTime());
        discuss.setUpdateTime(DateUtil.date().toLocalDateTime());
        discuss.setViewNum(0);
        discussMapper.insert(discuss);
    }

    /**
     * 修改讨论
     * @param discuss 包含 id 和要更新的字段
     */
    public void update(Discuss discuss) {
        if (discuss.getId() == null) {
            throw new CustomException("讨论ID不能为空");
        }
        Discuss db = discussMapper.selectById(discuss.getId());
        if (db == null) {
            throw new CustomException("讨论不存在");
        }
        // update_time 由数据库自动维护
        discussMapper.update(discuss);
    }

    /**
     * 删除讨论
     * @param id 讨论ID
     */
    public void deleteById(Integer id) {
        Discuss db = discussMapper.selectById(id);
        if (db == null) {
            throw new CustomException("讨论不存在");
        }
        discussMapper.deleteById(id);
    }

    /**
     * 查询所有讨论（倒序，不分页）
     */
    public List<Discuss> selectAll() {
        return discussMapper.selectAll();
    }

    /**
     * 根据标题模糊查询讨论列表
     * @param title 标题关键字
     */
    public List<Discuss> selectByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new CustomException("搜索关键字不能为空");
        }
        return discussMapper.selectByTitle(title.trim());
    }
}
