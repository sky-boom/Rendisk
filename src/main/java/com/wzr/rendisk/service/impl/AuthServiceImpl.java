package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.core.GlobalException;
import com.wzr.rendisk.core.ResultCode;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.UserMapper;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wzr
 * @date 2023-06-02 20:21
 */
@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean register(String username, String password, String nickname) {
        if (StringUtils.isAnyEmpty(username, password)) {
            throw new GlobalException(ResultCode.FIELD_NULL);
        }
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            throw new GlobalException(ResultCode.USER_USERNAME_EMPTY);
        }
        // 验证通过，插入用户
        User newUser = new User();
        if (StringUtils.isEmpty(nickname)) {
            nickname = UserUtils.getRandomNickname();
        }
        newUser.setNickname(nickname);
        newUser.setUsername(username);
        newUser.setPassword(UserUtils.encryptPassword(password));
        int resultRows = userMapper.insertUser(newUser);
        // 疑惑：会有不抛出异常而未向数据库插入数据的情况吗？
        return resultRows != 0;
    }
}
