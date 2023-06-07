package com.wzr.rendisk.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 工具类
 * @author wzr
 * @date 2023-06-01 23:21
 */
public class HttpUtils {

    /**
     * 用于获取当前请求的url
     * 参考：https://blog.csdn.net/PacosonSWJTU/article/details/121502765
     * @return url
     */
    public static String getHttpRequestUrlPath() {
        return getCurrentHttpRequest().getRequestURI();
    }

    /**
     * 用于获取当前请求的 request
     * @return request
     */
    public static HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
    }

    /**
     * 用于获取当前请求的 response
     * @return response
     */
    public static HttpServletResponse getCurrentHttpResponse() {
        return ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getResponse();
    }
}
