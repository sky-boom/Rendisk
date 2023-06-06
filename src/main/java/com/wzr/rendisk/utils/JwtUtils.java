package com.wzr.rendisk.utils;

import com.wzr.rendisk.config.AuthProperties;
import com.wzr.rendisk.core.constant.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * jwt 相关工具类
 * 1. 生成token
 * @author wzr
 * @date 2023-06-04 18:26
 */
public class JwtUtils {
    
    private static final AuthProperties authProperties = SpringContextHolder.getBean(AuthProperties.class);

    /**
     * 获取解析后的数据体，交给业务逻辑处理。
     * 此处不处理jwt过期问题。
     * @param jwtToken
     * @return
     */
    public static Claims parse(String jwtToken) {
        try {
            return Jwts.parser().setSigningKey(authProperties.getJwtSecretKey())
                    .parseClaimsJws(jwtToken).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JwtException("jwt解析失败!");
        }
    }
}
