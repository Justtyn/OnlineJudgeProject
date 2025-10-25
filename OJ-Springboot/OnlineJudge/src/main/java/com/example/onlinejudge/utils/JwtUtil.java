package com.example.onlinejudge.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    // 秘钥（建议在生产环境中存放在安全地方）
    // 这个秘钥是用来进行签名和验证 token 的，需要保密。生产环境中不应硬编码到代码中，应该通过安全的方式存储。
    private static final String SECRET_KEY = "ahdigf0239hf28hf274g87923f8098hg9hf2309uhroOIHHihoihHILHIPjh9hahaashwjdiqwhidiqhdidhqidhiqdhihdqidiqdq02rfhhf38";
    
    // Token 过期时间，例如 1 天（毫秒）
    // 这里设置了 Token 的过期时间为 24 小时，单位为毫秒。
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
//    private static final long EXPIRATION_TIME = 1000;

    // 生成 token（这里可以加入更多 payload 信息，如用户角色、ID 等）
    // 该方法根据给定的用户名生成一个 JWT Token。Token 中包含了用户名和过期时间等信息，且通过 SECRET_KEY 进行签名。
    public static String generateToken(String username) {
        return Jwts.builder()  // 使用 Jwts 库的 builder 方法来构建 JWT Token
                .setSubject(username)                         // 设置主题为用户名，作为 token 的 payload 一部分
                .setIssuedAt(new Date())                        // 设置签发时间，当前时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置 Token 的过期时间，当前时间 + 24 小时
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // 使用 HS512 算法和秘钥进行签名，确保 Token 的安全性
                .compact();  // 完成 Token 构建
    }

    // 解析 token，返回 token 中的声明信息（Claims）
    // 该方法将解析传入的 JWT Token，并通过秘钥对其进行验证。返回的是 Token 中的 Claims（声明），通常包括用户名、过期时间等信息。
    public static Claims parseToken(String token) {
        return Jwts.parser()  // 使用 Jwts 库的 parser 方法来解析 JWT Token
                .setSigningKey(SECRET_KEY) // 设置签名时使用的秘钥，确保 Token 的有效性
                .parseClaimsJws(token) // 解析传入的 Token，返回 Claims 对象
                .getBody();  // 从解析结果中提取出 Claims 的主体部分
    }
}



