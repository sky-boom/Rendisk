package com.wzr.rendisk.config.mvc;

import com.alibaba.fastjson.JSONObject;
import com.wzr.rendisk.config.redis.RedisClient;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.utils.JwtUtils;
import com.wzr.rendisk.utils.HttpUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * 日志打印切面类
 * @author wzr
 * @date 2023-06-07 0:31
 */
@Component
@Slf4j
@Aspect
public class WebLogAspect {

    /** 用于接口计时 */
    ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    
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
        startTime.set(System.currentTimeMillis());
        
        // 接收到请求，记录请求内容
        log.info("");
        log.info("—————————————————————— start ——————————————————————");
        log.info("[ThreadLocal计时] 线程{}开始计时", Thread.currentThread().getName());
        log.info("\tWebLogAspect.doBefore()");
        // 记录下请求内容
        log.info("\tURL : " + HttpUtils.getHttpRequestUrlPath());
        String token = HttpUtils.getCurrentHttpRequest().getHeader(JwtConstant.JWT_HEADER_NAME);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = JwtUtils.parse(token);
                String username = claims.getSubject();
                long timestamp = ((Integer) claims.get(JwtConstant.JWT_TIMESTAMP_FIELD)).longValue();
                long redisTimestamp = ((Integer) redisClient.getKey(JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + username)).longValue();
                log.info("\tJWT : username={}, jwtTimestamp={}, redisTimestamp={}", username, timestamp, redisTimestamp);
            } catch (Exception e) {
                log.error("\t解析jwt时出现错误。");
            }
        } else {
            log.info("\t请求头中未检查到有jwt");
        }
        HttpServletRequest request = HttpUtils.getCurrentHttpRequest();
        //获取全部请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        int i = 1;
        for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {
            log.info("\t参数{} : {} = {}", i ++,  entry.getKey(), Arrays.toString(entry.getValue()) );
        }  
//        log.info("——————————————————————  end ———————————————————————");
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) {
//        log.info("返回参数: {}", JSONObject.toJSONString(ret));
        HttpServletRequest request = HttpUtils.getCurrentHttpRequest();
        log.info("接口 {} 访问完毕" , request.getRequestURL());
        log.info("[ThreadLocal计时] 线程{}结束计时，共耗时（毫秒）: {}" , Thread.currentThread().getName() ,(System.currentTimeMillis() - startTime.get()));
        log.info("——————————————————————— end ———————————————————————");
    }
}
