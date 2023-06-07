package com.wzr.rendisk.config.shiro;

import com.wzr.rendisk.config.redis.RedisClient;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.ITokenService;
import com.wzr.rendisk.utils.JwtUtils;
import com.wzr.rendisk.utils.HttpUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自定义Shiro的Realm，执行真正的token认证 + 权限认证。
 * @author wzr
 * @date 2023-06-05 17:13
 */
@Slf4j
@Component
public class JwtRealm extends AuthorizingRealm {
    
    @Autowired
    private IAuthService authService;
    
    @Autowired
    private RedisClient redisClient;
    
    @Autowired
    private ITokenService tokenService;
    
    /**
     * 据说不重写会报错。
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof TokenModel;
    }
    
    /**
     * 权限认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 对给定的AccessToken进行认证
     * 1. 如果解析失败，会一路抛出异常。
     * 2. 如果AccessToken已过期，在此处抛 ExpiredJwtException 异常
     * 3. 拿到Token携带的用户名和时间戳，并判断相关的 RefreshToken 是否存在于Redis中
     *      Redis验证失败，说明当前Token无效，抛出认证失败异常。
     *      Redis验证成功，但已过期，则续签。
     * 
     * @param authToken 自定义的TokenModel
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
        TokenModel token = (TokenModel) authToken;
        // 进行jwt解析，验证jwt结构的完整性（包括是否到期）
        Claims parse = JwtUtils.parse(token.getJwtToken());
        // 拿到用户名和时间戳，进行相关验证
        String username = parse.getSubject();
        long jwtTimestamp = Long.parseLong( String.valueOf( parse.get(JwtConstant.JWT_TIMESTAMP_FIELD) ) );
        // 验证用户名是否存在
        if (authService.getUserByUsername(username) != null) {
            String userRefreshKey = JwtConstant.PREFIX_SHIRO_REFRESH_TOKEN + username;
            // 验证 refreshToken 是否还存在于 redis 中
            if (redisClient.isKeyExists(userRefreshKey)) {
                long redisTimestamp = ((Integer) redisClient.getKey(userRefreshKey)).longValue();
                // 验证时间戳是否正确（token是否是从服务端发送的？）
                if (jwtTimestamp == redisTimestamp) {
                    String userAccessKey = JwtConstant.PREFIX_SHIRO_ACCESS_TOKEN + username;
                    // 验证accessToken是否过期
                    if (redisClient.isKeyExists(userAccessKey)) {
                        // accessToken 没过期，当前token继续交给shiro保管
                        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "userRealm");
                    } else {
                        // accessToken 过期了，则续签一个token，再交给shiro保管
                        String newToken = tokenService.refreshToken(username);
                        // 设置到请求头，返回给前端
                        HttpUtils.getCurrentHttpResponse().setHeader(JwtConstant.JWT_HEADER_NAME, newToken);
                        return new SimpleAuthenticationInfo(newToken, newToken, "userRealm");
                    }
                }
            }
        }
        throw new AuthenticationException("认证失败, 请重新登录");
    }
}
