package com.wzr.rendisk.config.mvc;

import com.wzr.rendisk.config.redis.RedisClient;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.utils.JwtUtils;
import com.wzr.rendisk.utils.HttpUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日志打印切面类
 * @author wzr
 * @date 2023-06-07 0:31
 */
@Component
@Slf4j
@Aspect
public class WebLogAspect {
    
    @Autowired
    private RedisClient redisClient;
    
    /**
     * 定义一个切入点. 解释下：
     * 第一个 * 代表任意修饰符及任意返回值. 
     * 第二个 * 任意包名 
     * 第三个 * 代表任意方法.
     * 第四个 * 定义在web包或者子包
     * 第五个 * 任意方法
     * ".." 表示匹配任意数量的参数. 
     */
    @Pointcut("execution(* com.wzr.rendisk.controller..*.*(..)) ")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        // 接收到请求，记录请求内容
        log.info("=========================================");
        log.info("WebLogAspect.doBefore()");
        // 记录下请求内容
        log.info("URL : " + HttpUtils.getHttpRequestUrlPath());
        String token = HttpUtils.getCurrentHttpRequest().getHeader(JwtConstant.JWT_HEADER_NAME);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = JwtUtils.parse(token);
                String username = claims.getSubject();
                long timestamp = ((Integer) claims.get(JwtConstant.JWT_TIMESTAMP_FIELD)).longValue();
                long redisTimestamp = ((Integer) redisClient.getKey(JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + username)).longValue();
                log.info("JWT : username={}, jwtTimestamp={}, redisTimestamp={}", username, timestamp, redisTimestamp);
            } catch (Exception e) {
                log.error("解析jwt时出现错误。");
            }
        } else {
            log.info("请求头中未检查到有jwt");
        }
        log.info("=========================================");
    }
    
}
