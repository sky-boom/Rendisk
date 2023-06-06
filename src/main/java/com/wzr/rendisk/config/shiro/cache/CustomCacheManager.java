package com.wzr.rendisk.config.shiro.cache;

import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wzr
 * @date 2023-06-06 13:14
 */
@Component
public class CustomCacheManager implements CacheManager {
    
    @Autowired
    private CustomCache customCache;
    
    @Override
    public <K, V> CustomCache getCache(String s) throws CacheException {
        return this.customCache;
    }
}