package com.wzr.rendisk.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 工具类
 * @author wzr
 * @date 2023-06-01 23:21
 */
public class Utils {

    /**
     * 用于获取当前请求的url
     * 参考：https://blog.csdn.net/PacosonSWJTU/article/details/121502765
     * @return url
     */
    public static String getRequestUrlPath() {
        HttpServletRequest request =
                ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        return request.getRequestURI();
    }
}
