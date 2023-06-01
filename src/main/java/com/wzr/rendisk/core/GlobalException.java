package com.wzr.rendisk.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局异常
 * @author wzr
 * @date 2023-06-01 22:02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlobalException extends RuntimeException {
    
    /** 
     * 全局异常只需传递错误的状态码
     * 因为任何情况下，返回给前端的永远是 ResultData
     */
    private final ResultCode resultCode;
    
    public GlobalException() {
        this.resultCode = ResultCode.ERROR;
    }
    public GlobalException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
