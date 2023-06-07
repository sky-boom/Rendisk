package com.wzr.rendisk.config.shiro.cache;

import com.wzr.rendisk.config.AuthProperties;
import com.wzr.rendisk.config.redis.RedisClient;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author wzr
 * @date 2023-06-06 13:05
 */
@Slf4j
@Component
public class CustomCache<K, V> implements Cache<K, V> {


    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AuthProperties authProperties;
    
    /**
     * 缓存的key名称获取为shiro:cache:{username}
     */
    private String getKey(Object key) {
        // 获取用户名
        String username = JwtUtils.parse(key.toString()).getSubject();
        return JwtConstant.PREFIX_SHIRO_CACHE + username;
    }

    /**
     * 获取缓存
     */
    @Override
    public Object get(Object key) throws CacheException {
        log.info("获得缓存: key = {}", key);
        String redisKey = this.getKey(key);
        if (redisClient.isKeyExists(redisKey)) {
            return redisClient.getKey(redisKey);
        }
        return null;
    }

    /**
     * 保存缓存
     */
    @Override
    public Object put(Object key, Object value) throws CacheException {
        log.info("保存缓存: <key, value> = <{}, {}>", key, value);
        // 读取配置文件，获取Redis的Shiro缓存过期时间
        long shiroCacheExpireTime = authProperties.getShiroCacheExpireTime();
        // 设置Redis的Shiro缓存
        return redisClient.set(this.getKey(key), value, shiroCacheExpireTime);
    }

    /**
     * 移除缓存
     */
    @Override
    public Object remove(Object key) throws CacheException {
        log.info("移除缓存: key = {}", key);
        String redisKey = this.getKey(key);
        if ( redisClient.isKeyExists(redisKey) ) {
            redisClient.delKey(redisKey);
        }
        return null;
    }

    /**
     * 清空所有缓存
     * 已特定前缀的key，全删了。
     */
    @Override
    public void clear() throws CacheException {
        log.info("清空所有缓存");
        redisClient.delByPrefix(JwtConstant.PREFIX_SHIRO_CACHE);
    }

    /**
     * 缓存的个数
     */
    @Override
    public int size() {
        log.debug("得到缓存个数");
        return redisClient.sizeByPrefix(JwtConstant.PREFIX_SHIRO_CACHE);
    }

    /**
     * 获取所有的key
     */
    @Override
    public Set keys() {
        Set<Object> keys = redisClient.getKeyByPrefix(JwtConstant.PREFIX_SHIRO_CACHE);
        log.info("获得缓存: keys = {}", keys);
        return keys;
    }

    /**
     * 获取所有的value
     */
    @Override
    public Collection values() {
        List<Object> values = redisClient.getValByPrefix(JwtConstant.PREFIX_SHIRO_CACHE);
        log.info("获得缓存: values = {}", values);
        return values;
    }
}