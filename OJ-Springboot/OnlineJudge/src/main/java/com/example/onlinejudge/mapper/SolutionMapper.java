package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Solution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SolutionMapper extends BaseMapper<Solution> {
    
    @Update("UPDATE solution SET `like_num` = `like_num` + 1 WHERE id = #{id}")
    int increaseLike(Integer id);
}
