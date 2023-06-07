package com.wzr.rendisk.service;

import com.wzr.rendisk.dto.UserDto;
import com.wzr.rendisk.entity.User;
import org.apache.shiro.subject.Subject;

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

    /**
     * 通过 Shiro 的 subject 对象，找到当前登录的用户。
     * 原理是，subject中存有当前用户的jwtToken，可以从中解析出用户名，然后查找数据库
     * @param subject Shiro主体
     * @return User
     */
     User getCurrentUser(Subject subject);
}
