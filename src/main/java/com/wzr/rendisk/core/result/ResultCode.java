package com.wzr.rendisk.core.result;

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

    /** 用户名已存在 (409, 存在冲突) */
    USER_USERNAME_EXIST(1005, 409, "用户名已存在! "),
    
    /** 用户名或密码错误 (404, 资源未找到) */
    USERNAME_PASSWORD_INCORRECT(1006, 404, "用户名或密码错误! "),

    /** jwt失效或解析失败，尝试重新登录 (401, 需要身份认证验证) */
    NEED_TO_LOGIN(1007, 401, "认证信息失效，请重新登录! "),

    /** 游客非法访问接口时的错误 (401, 需要身份认证验证) */
    GUEST_NEED_TO_LOGIN(1008, 401, "无权访问，请先登录用户! "),



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
