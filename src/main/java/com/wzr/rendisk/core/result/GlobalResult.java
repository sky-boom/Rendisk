package com.wzr.rendisk.core.result;

/**
 * 包装返回的结果
 * @author wzr
 * @date 2023-06-01 21:11
 */
public class GlobalResult {

    /**
     * 获取成功的请求结果（无数据）
     * @return ResultData
     */
    public static ResultData<Object> success() {
        return new ResultData<>(ResultCode.SUCCESS, null);
    }
    
    /**
     * 获取成功的请求结果（有数据）
     * @param data 需返回的数据
     * @param <T> 数据类型
     * @return ResultData<T>
     */
    public static <T> ResultData<T> success(T data) {
        return new ResultData<>(ResultCode.SUCCESS, data);
    }

    /**
     * 获取失败的请求结果（默认状态码）
     * @return ResultData
     */
    public static ResultData<?> error() {
        return new ResultData<>(ResultCode.ERROR, null);
    }

    /**
     * 获取失败的请求结果（规定状态码）
     * @return ResultData<T>
     */
    public static ResultData<?> error(ResultCode resultCode) {
        return new ResultData<>(resultCode, null);
    }
    
}
