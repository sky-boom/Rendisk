package com.wzr.rendisk.service;

/**
 * shiro + jwt + redis认证时涉及到的相关方法。
 * 
 * @author wzr
 * @date 2023-06-05 17:19
 */
public interface ITokenService {
    /**
     * 根据用户名来生成一个jwt (accessToken)
     * 并在redis中设置refreshToken及其过期时间。
     * @param username 用户名
     * @return jwt
     */
    String createToken(String username);

    /**
     * 根据用户名来续签一个jwt (accessToken)
     * 并在redis中设置refreshToken
     * @param username 用户名
     * @return jwt
     */
    String refreshToken(String username);
}
