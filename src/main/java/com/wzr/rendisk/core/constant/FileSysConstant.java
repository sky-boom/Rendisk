package com.wzr.rendisk.core.constant;

/**
 * @author wzr
 * @date 2023-06-07 22:05
 */
public class FileSysConstant {

    /**
     * 文件命名不允许出现的字符
     */
    public static final String NAME_EXCEPT_SYMBOL = ".*[\\?\\*:\"<>\\/\\|].*";

    /**
     * 标记是文件操作（数值和前端保持一致）
     */
    public static final Integer FILE_TYPE = 1;

    /**
     * 标记是目录操作（数值和前端保持一致）
     */
    public static final Integer FOLDER_TYPE = 0;
}
