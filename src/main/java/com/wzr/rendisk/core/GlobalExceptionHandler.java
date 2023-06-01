package com.wzr.rendisk.core;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wzr
 * @date 2023-06-01 23:26
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截全局异常，返回错误信息
     * @param ex 拦截到的异常
     * @return 错误信息
     */
    @ExceptionHandler(GlobalException.class)
    public ResultData<?> globalHandler(GlobalException ex) {
        return GlobalResult.error(ex.getResultCode());
    }
    
}
