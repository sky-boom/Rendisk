package com.wzr.rendisk.core;

/**
 * 结果状态码通用类。返回内容：
 *  1. 消息类型唯一标识
 *  2. HTTP状态码 (参考 https://blog.csdn.net/weixin_57467236/article/details/126378061)
 *  3. 状态消息
 * @author wzr
 * @date 2023-6-1 18点26分
 */

public enum ResultCode {

    /** 资源未找到 404 */
    RESOURCE_NOT_FOUND(1001, 404, "未找到该资源"),
    /** 请求成功 200 */
    SUCCESS(1002, 200, "请求成功"),
    /** 服务器未能响应 (500, 服务器端程序错误) */
    ERROR(1003, 500, "服务器跑到外太空了"),
    
    /*-------------------------- 普通 ---------------------------*/
    
    /** 前端传递的查询参数为空 (412, 请求未带条件) */
    FIELD_NULL(1004, 412, "请输入正确的内容! "),

    /*-------------------------- 用户 ---------------------------*/

    /** 前端传递的查询参数为空 (409, 存在冲突) */
    USER_USERNAME_EMPTY(1005, 409, "用户名已存在! "),
    
    
    ;
    /** 1. 消息类型唯一标识 */
    private final int code;
    /** 2. HTTP状态码 */
    private final int status;
    /** 3. 状态消息 */
    private final String message;
    
    ResultCode(int code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return"ErrorCode{" +
                "code=" + code +
                ", status=" + status +
                ", msg='" + message + '\'' +
                '}';
    }
}
