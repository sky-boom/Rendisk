package com.wzr.rendisk.core;

import com.wzr.rendisk.utils.Utils;
import lombok.Data;

import java.time.Instant;

/**
 * 给前端的响应数据
 * @author wzr
 * @date 2023-06-01 18:40
 */
@Data
public class ResultData<T> {
    private int code;
    private int status;
    private String message;
    
    /** 返回的数据 */
    private T data;
    /** 请求的接口url路径 */
    private String path;
    /** 时间戳 */
    private Instant timestamp;
    
    public ResultData(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.status = resultCode.getStatus();
        this.message = resultCode.getMessage();
        this.data = data;
        this.path = Utils.getRequestUrlPath();
        this.timestamp = Instant.now();
    }


}
