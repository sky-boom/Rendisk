package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.config.AuthProperties;
import com.wzr.rendisk.config.RedisClient;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.service.ITokenService;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * shiro + jwt + redis认证时涉及到的相关方法。
 * 
 * @author wzr
 * @date 2023-06-05 17:20
 */
@Slf4j
@Service
public class TokenServiceImpl implements ITokenService {
    
    @Autowired
    private AuthProperties authProperties;
    
    @Autowired
    private RedisClient redisClient;
    
    @Override
    public String createToken(String username) {
        log.info("开始生成用户{}的token...", username);
        // 当前时间实例
        Instant currentTime = Instant.now();
        // 记录时间戳，用于accessToken和refreshToken的安全认证。注意，此方法涉及时间的，都是秒数。
        long currentTimestamp = currentTime.getEpochSecond();
        // Token在redis中的键。
        String refreshTokenKey = JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + username;
        String accessTokenKey = JwtConstant.PREFIX_SHIRO_ACCESS_TOKEN + username;
        redisClient.set(refreshTokenKey, currentTimestamp, authProperties.getRefreshTokenExpireTime());
        redisClient.set(accessTokenKey, currentTimestamp, authProperties.getAccessTokenExpireTime());
        /*
           根据用户名，创建一个jwt并返回： 
              header: {
                "typ": "JWT",
                "alg": "HS256"  // 签名算法
              },
              payload: {
                "sub": username     // 主题，设置为用户名
                "id": UUID          // jwt的id，标识唯一jwt
                "timestamp": currentTime  // 用这个字段表示生成令牌的时间。它虽然内置了setIssuedAt方法，但要Date类型，不好用。
                "exp": 当前时间戳 + accessToken-expireTime  // 过期时间
              }
         */
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", SignatureAlgorithm.HS256.getValue())
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim(JwtConstant.JWT_TIMESTAMP_FIELD, currentTimestamp)
                .signWith(SignatureAlgorithm.HS256, authProperties.getJwtSecretKey())
                .compact();
    }

    @Override
    public String refreshToken(String username) {
        log.info("用户{}的accessToken已过期，准备重新生成...", username);
        // 当前时间实例
        Instant currentTime = Instant.now();
        long currentTimestamp = currentTime.getEpochSecond();
        // 更新 redis refreshToken 中的时间戳 
        String userRefreshKey = JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + username;
        long refreshExpire = redisClient.getExpire(userRefreshKey);
        redisClient.set(userRefreshKey, currentTimestamp, refreshExpire);
        // 创建 redis accessToken
        String accessTokenKey = JwtConstant.PREFIX_SHIRO_ACCESS_TOKEN + username;
        redisClient.set(accessTokenKey, currentTimestamp, authProperties.getAccessTokenExpireTime());
        // 创建新的 jwt accessToken 并返回
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", SignatureAlgorithm.HS256.getValue())
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim(JwtConstant.JWT_TIMESTAMP_FIELD, currentTimestamp)
                .signWith(SignatureAlgorithm.HS256, authProperties.getJwtSecretKey())
                .compact();
    }
}
