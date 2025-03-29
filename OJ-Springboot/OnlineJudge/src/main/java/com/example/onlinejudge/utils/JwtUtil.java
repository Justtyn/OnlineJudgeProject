package com.example.onlinejudge.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    // 秘钥（建议在生产环境中存放在安全地方）
    private static final String SECRET_KEY = "ahdigf0239hf28hf274g87923f8098hg9hf2309uhroOIHHihoihHILHIPjh9hahaashwjdiqwhidiqhdidhqidhiqdhihdqidiqdq02rfhhf38";
    // Token 过期时间，例如 1 天（毫秒）
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 生成 token（这里可以加入更多 payload 信息，如用户角色、ID 等）
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)                         // 设置主题为用户名
                .setIssuedAt(new Date())                        // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // 使用 HS512 算法签名
                .compact();
    }

    // 解析 token，返回 token 中的声明信息（Claims）
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // 使用秘钥解析
                .parseClaimsJws(token)
                .getBody();
    }
}



