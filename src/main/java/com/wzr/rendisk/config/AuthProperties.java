package com.wzr.rendisk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 读取配置文件中，和认证“Auth”相关的自定义配置
 * @author wzr
 * @date 2023-06-05 19:10
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "my-config.auth")
public class AuthProperties {
    
    private String jwtSecretKey;
    
    private long accessTokenExpireTime;
    
    private long refreshTokenExpireTime;
    
    private long shiroCacheExpireTime;
}
