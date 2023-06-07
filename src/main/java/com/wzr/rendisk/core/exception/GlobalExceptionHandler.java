package com.wzr.rendisk.core.exception;

import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.core.result.ResultData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
     * 参数校验错误异常
     * @param ex 拦截到的异常
     * @return 错误参数信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
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
