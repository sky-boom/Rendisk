package com.wzr.rendisk.service;

/**
 * 认证业务逻辑
 * @author wzr
 * @date 2023-06-02 20:21
 */
public interface IAuthService {

    /**
     * 注册
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @return 是否注册成功
     */
    boolean register(String username, String password, String nickname);
}
