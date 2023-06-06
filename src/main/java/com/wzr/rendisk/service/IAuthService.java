package com.wzr.rendisk.service;

import com.wzr.rendisk.dto.UserDto;
import com.wzr.rendisk.entity.User;

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

    /**
     * 根据用户名获取用户
     * @param username 用户名（唯一）
     * @return User
     */
    User getUserByUsername(String username);

    /**
     * 登录
     * @param username 用户名
     * @param plainPassword 前端明文密码
     * @return 用户基本信息
     */
    UserDto login(String username, String plainPassword);
}
