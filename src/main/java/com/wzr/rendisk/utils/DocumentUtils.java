package com.wzr.rendisk.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * 文件工具类
 * @author wzr
 * @date 2023-06-10 0:07
 */
public class DocumentUtils {

    /**
     * 通过文件的二进制数据，转化成Base64并返回
     * @param bytes 文件二进制数据
     * @return Base64内容
     */
    public static String getBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获得文件后缀名
     * @param fileName 文件名
     * @return 后缀名（不含点）
     */
    public static String getFileExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    /**
     * inputStream转outputStream
     * @return
     */
    public static OutputStream input2Output(final InputStream in) throws Exception {
        final ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            swapStream.write(ch);
        }
        return swapStream;
    }
}
