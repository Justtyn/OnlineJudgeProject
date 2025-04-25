// DiscussController.java
package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Discuss;
import com.example.onlinejudge.service.DiscussService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discuss")
public class DiscussController {

    @Resource
    private DiscussService discussService;

    /**
     * 分页倒序查询讨论列表
     * @param pageNum 页码，默认 1
     * @param pageSize 每页数量，默认 10
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> map = discussService.selectPage(pageNum, pageSize);
        return Result.success(map);
    }

    /**
     * 查询单条讨论详情（含评论树）
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Discuss d = discussService.selectById(id);
        return Result.success(d);
    }

    /**
     * 新增讨论
     */
    @PostMapping
    public Result add(@RequestBody Discuss discuss) {
        discussService.add(discuss);
        return Result.success("添加成功");
    }

    /**
     * 修改讨论
     */
    @PutMapping
    public Result update(@RequestBody Discuss discuss) {
        discussService.update(discuss);
        return Result.success("修改成功");
    }

    /**
     * 删除讨论
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        discussService.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 查询所有讨论（不分页）
     */
    @GetMapping("/all")
    public Result all() {
        List<Discuss> list = discussService.selectAll();
        return Result.success(list);
    }

    /**
     * 根据标题模糊查询讨论列表
     */
    @GetMapping("/search")
    public Result search(@RequestParam String title) {
        List<Discuss> list = discussService.selectByTitle(title);
        return Result.success(list);
    }
}
