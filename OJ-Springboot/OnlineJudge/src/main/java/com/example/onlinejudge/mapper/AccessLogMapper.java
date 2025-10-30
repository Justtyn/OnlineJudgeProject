package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccessLogMapper {
    int insert(AccessLog log);

    int batchInsert(@Param("list") List<AccessLog> list);

    List<AccessLog> selectByFilter(@Param("from") java.util.Date from,
                                   @Param("to") java.util.Date to,
                                   @Param("method") String method,
                                   @Param("pathLike") String pathLike,
                                   @Param("status") Integer status,
                                   @Param("minDuration") Integer minDuration,
                                   @Param("maxDuration") Integer maxDuration,
                                   @Param("clientIp") String clientIp,
                                   @Param("username") String username,
                                   @Param("isAlert") Boolean isAlert,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    long countByFilter(@Param("from") java.util.Date from,
                       @Param("to") java.util.Date to,
                       @Param("method") String method,
                       @Param("pathLike") String pathLike,
                       @Param("status") Integer status,
                       @Param("minDuration") Integer minDuration,
                       @Param("maxDuration") Integer maxDuration,
                       @Param("clientIp") String clientIp,
                       @Param("username") String username,
                       @Param("isAlert") Boolean isAlert);
}


