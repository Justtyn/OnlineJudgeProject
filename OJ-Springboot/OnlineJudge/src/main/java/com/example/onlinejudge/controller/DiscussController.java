// DiscussController.java
package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.entity.Discuss;
import com.example.onlinejudge.service.DiscussService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "讨论管理接口")
@RestController
@RequestMapping("/discuss")
public class DiscussController {

    @Resource
    private DiscussService discussService;

    @ApiOperation("分页查询讨论列表")
    @GetMapping("/page")
    public Result page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> map = discussService.selectPage(pageNum, pageSize);
        return Result.success(map);
    }

    @ApiOperation("查询讨论详情")
    @GetMapping("/{id}")
    public Result getById(@ApiParam("讨论ID") @PathVariable Integer id) {
        Discuss d = discussService.selectById(id);
        return Result.success(d);
    }

    @ApiOperation("新增讨论")
    @PostMapping
    public Result add(@ApiParam("讨论信息") @RequestBody Discuss discuss) {
        discussService.add(discuss);
        return Result.success("添加成功");
    }

    @ApiOperation("修改讨论")
    @PutMapping
    public Result update(@ApiParam("讨论信息") @RequestBody Discuss discuss) {
        discussService.update(discuss);
        return Result.success("修改成功");
    }

    @ApiOperation("删除讨论")
    @DeleteMapping("/{id}")
    public Result delete(@ApiParam("讨论ID") @PathVariable Integer id) {
        discussService.deleteById(id);
        return Result.success("删除成功");
    }

    @ApiOperation("查询所有讨论")
    @GetMapping("/all")
    public Result all() {
        List<Discuss> list = discussService.selectAll();
        return Result.success(list);
    }

    @ApiOperation("根据标题搜索讨论")
    @GetMapping("/search")
    public Result search(@ApiParam("讨论标题") @RequestParam String title) {
        List<Discuss> list = discussService.selectByTitle(title);
        return Result.success(list);
    }
}
