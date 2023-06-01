package com.wzr.rendisk.core;

import com.wzr.rendisk.utils.Utils;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
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
    private String msg;
    
    /** 返回的数据 */
    private T data;
    /** 请求的接口url路径 */
    private String path;
    /** 时间戳 */
    Instant timestamp;
    
    public ResultData(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.status = resultCode.getStatus();
        this.msg = resultCode.getMsg();
        this.data = data;
        this.path = Utils.getCurrentUrlPath();
        this.timestamp = Instant.now();
    }


}
