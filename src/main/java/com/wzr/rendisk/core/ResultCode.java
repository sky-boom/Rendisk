package com.wzr.rendisk.core;

import org.springframework.http.HttpStatus;

/**
 * 结果状态码通用类。返回内容：
 *  1. 消息类型唯一标识
 *  2. HTTP状态码
 *  3. 状态消息
 * @author wzr
 * @date 2023-6-1 18点26分
 */

public enum ResultCode {

    /** 资源未找到 404 */
    RESOURCE_NOT_FOUND(1001, HttpStatus.NOT_FOUND.value(), "未找到该资源"),
    /** 请求成功 200 */
    SUCCESS(1002, HttpStatus.OK.value(), "请求成功"),
    ERROR(1003, HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器跑到外太空了")
    
    ;
    /** 1. 消息类型唯一标识 */
    private final int code;
    /** 2. HTTP状态码 */
    private final int status;
    /** 3. 状态消息 */
    private final String msg;
    
    ResultCode(int code, int status, String msg) {
        this.code = code;
        this.status = status;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return"ErrorCode{" +
                "code=" + code +
                ", status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
