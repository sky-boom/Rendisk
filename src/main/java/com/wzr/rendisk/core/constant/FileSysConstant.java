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

    /**
     * 搜索文档，以团队为单位
     */
    public static final Integer SEARCH_BY_TEAM = 1;

    /**
     * 搜索文档，以网站中公开的文档为单位
     */
    public static final Integer SEARCH_BY_ALL = 2;
}
