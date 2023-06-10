package com.wzr.rendisk.config.redis;

import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 键过期监听
 * 参考：https://blog.csdn.net/leilei1366615/article/details/109280512
 * 
 * @author wzr
 * @date 2023-06-10 11:43
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener implements DisposableBean {
    
    @Autowired
    private IAuthService authService;
    @Autowired
    private ISearchService searchService;
    @Autowired
    private RedisClient redisClient;
    
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }
    
    
    /**
     * 针对 redis 数据失效事件，进行数据处理
     *
     * @param message key
     * @param pattern pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取到失效的 key
        String expiredKey = message.toString();
        log.info("[redis] 键[{}]已过期", expiredKey);
        // 获取当前用户，执行登出操作
        User user = authService.getCurrentUser(SecurityUtils.getSubject());
        String refreshKey = JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + user.getUsername();
        if (expiredKey.equals(refreshKey)) {
            // 当前用户已过期，删除对应用户的es文件内容
//            searchService.removeAllFile(user);
//            log.info("[redis] 监听到用户token已失效，准备清空用户es文档");
        }
        super.onMessage(message, pattern);
    }

    /**
     * 在未知原因关闭项目时，用户token信息会删除，ES内容也会被删除
     * 注意：此方法并未生效！
     */
    @Override
    public void destroy() {
//        User user = authService.getCurrentUser(SecurityUtils.getSubject());
//        String accessKey = JwtConstant.PREFIX_SHIRO_ACCESS_TOKEN + user.getUsername();
//        String refreshKey = JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + user.getUsername();
//        redisClient.delKey(accessKey, refreshKey);
//        log.info("[destroy-redis] 销毁用户token");
//        searchService.removeAllFile(user);
//        log.info("[destroy-es] 销毁用户es文档");
    }
}
