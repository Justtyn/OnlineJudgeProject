package com.example.onlinejudge.exception;

import com.example.onlinejudge.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result handleCustomException(CustomException e) {
        log.error("自定义异常：{}", e.getMessage());
        return Result.error("500", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("异常信息：", e);
        return Result.error("500", "系统错误");
    }
}
