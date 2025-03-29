package com.example.onlinejudge.entity;

import lombok.Data;

@Data
public class Account {
    private String username;
    private String password;
    private String role;

    private String name;
    private String sex;
    private String birth;
    private String phone;
    private String email;
    private String avatar;

    private String token;
}
