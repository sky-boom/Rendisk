package com.wzr.rendisk.config.mvc;

import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义controller中的用户参数
 * 即在Controller中添加 User参数的自动注入和校验。
 * @author wzr
 * @date 2023-06-07 21:24
 */
@Component
@Slf4j
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private IAuthService authService;
    
    /**
     * 判断参数类型是否为用户，只有该方法为true时，才会执行下面返回数据的方法
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    /**
     * 获取当前用户的逻辑
     * @return
     * @throws GlobalException 全局异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws GlobalException {
        // 获取正在登录的用户
        User user = authService.getCurrentUser( SecurityUtils.getSubject() );
        // 用户为空，提示登录
        if (user == null) {
            throw new GlobalException(ResultCode.USER_EMPTY);
        }
        return user;
    }
}
