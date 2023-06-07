package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.dto.UserDto;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.UserMapper;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.ITokenService;
import com.wzr.rendisk.utils.JwtUtils;
import com.wzr.rendisk.utils.UserUtils;
import com.wzr.rendisk.utils.HttpUtils;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * @author wzr
 * @date 2023-06-02 20:21
 */
@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ITokenService tokenService;

    @Override
    public boolean register(String username, String password, String nickname) {
        if (StringUtils.isAnyEmpty(username, password)) {
            throw new GlobalException(ResultCode.FIELD_NULL);
        }
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            throw new GlobalException(ResultCode.USER_USERNAME_EXIST);
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

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public UserDto login(String username, String plainPassword) {
        if (StringUtils.isAnyEmpty(username, plainPassword)) {
            throw new GlobalException(ResultCode.FIELD_NULL);
        }
        // 判断相应用户是否存在，以及密码是否正确
        User user = userMapper.selectByUsername(username);
        if (user != null && UserUtils.validatePassword(plainPassword, user.getPassword()) ) {
            // 验证通过，可以颁发jwt
            String jwtToken = tokenService.createToken(username);
            HttpServletResponse response = HttpUtils.getCurrentHttpResponse();
            response.setHeader(JwtConstant.JWT_HEADER_NAME, jwtToken);
            UserDto userDto = new UserDto();
            userDto.setUsername(username);
            userDto.setNickname(user.getNickname());
            userDto.setAvatarUrl(user.getAvatarUrl());
            userDto.setLastLoginTime(user.getLastLoginTime());
            return userDto;
        }
        throw new GlobalException(ResultCode.USERNAME_PASSWORD_INCORRECT);
    }

    @Override
    public User getCurrentUser(Subject subject) {
        String jwtToken = (String) subject.getPrincipal();
        Claims parse = JwtUtils.parse(jwtToken);
        return getUserByUsername(parse.getSubject());
    }
}
