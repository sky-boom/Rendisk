package com.wzr.rendisk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期相关工具类
 * @author wzr
 * @date 2023-06-07 20:08
 */
public class DateUtils {
    /**
     * 获取指定格式的当前时间
     * @param format 例如: yyyy/MM/dd
     * @return 例如: 2023/6/7
     */
    public static String getCurrFormatDateStr(String format) {
        // 获取当前时间
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    public static void main(String[] args) {
        System.out.println(getCurrFormatDateStr("yyyy/MM/dd"));
    }
}
