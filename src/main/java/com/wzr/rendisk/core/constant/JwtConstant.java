package com.wzr.rendisk.core.constant;

/**
 * jwt 相关常量
 * @author wzr
 * @date 2023-06-04 20:14
 */
public class JwtConstant {
    
    /** 
     * 请求头中用于标识jwt的名字 - Authorization
     */
    public static final String JWT_HEADER_NAME = "Authorization";

    /**
     * access_token对应键
     * redis-key-前缀-shiro:access_token:
     */
    public static final String PREFIX_SHIRO_ACCESS_TOKEN = "shiro:access_token:";
    
    /**
     * refresh_token对应键
     * redis-key-前缀-shiro:refresh_token:
     */
    public static final String PREFIX_SHIRO_REFRESH_TOKEN = "shiro:refresh_token:";

    /**
     * shiro自定义缓存键
     * redis-key-前缀-shiro:cache:
     */
    public static final String PREFIX_SHIRO_CACHE = "shiro:cache:";


    /**
     * jwt的payload域中，用 "timestamp" 字段表示token生成时间。
     */
    public static final String JWT_TIMESTAMP_FIELD = "timestamp";
}
