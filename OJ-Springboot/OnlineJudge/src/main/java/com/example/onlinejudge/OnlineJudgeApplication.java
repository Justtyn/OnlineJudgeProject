package com.example.onlinejudge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.onlinejudge.mapper")
@EnableScheduling
public class OnlineJudgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineJudgeApplication.class, args);
    }

}
