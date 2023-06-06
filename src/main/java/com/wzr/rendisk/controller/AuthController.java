package com.wzr.rendisk.controller;

import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.dto.UserDto;
import com.wzr.rendisk.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证接口
 * @author wzr
 * @date 2023-06-02 18:43
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;
    
    /**
     * 用户注册接口
     * @return 是否成功
     */
    @PostMapping("/register")
    public ResultData<Boolean> register(String username, String password, String nickname) {
        boolean result = authService.register(username, password, nickname);
        return GlobalResult.success(result);
    }

    /**
     * 用户登录接口
     * @return 成功，返回token信息。
     */
    @PostMapping("/login")
    public ResultData<UserDto> register(String username, String password) {
        UserDto userDto = authService.login(username, password);
        return GlobalResult.success(userDto);
    }
}
