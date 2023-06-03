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
     * 拦截自定义抛出的异常，返回错误信息
     * @param ex 拦截到的异常
     * @return 错误信息
     */
    @ExceptionHandler(GlobalException.class)
    public ResultData<?> globalHandler(GlobalException ex) {
        ex.printStackTrace();
        ResultCode resultCode = ex.getResultCode();
        if (resultCode == null) {
            return GlobalResult.error();
        }
        return GlobalResult.error(ex.getResultCode());
    }

    /**
     * 拦截所有异常，返回错误信息
     * @param ex 拦截到的异常
     * @return 错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResultData<?> globalHandler(Exception ex) {
        ex.printStackTrace();
        return GlobalResult.error();
    }
    
}
