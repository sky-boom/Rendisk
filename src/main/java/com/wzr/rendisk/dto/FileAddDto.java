package com.wzr.rendisk.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 前端添加文件时传来的参数
 * @author wzr
 * @date 2023-06-07 11:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FileAddDto {
    /**
     * 具体上传的文件
     */
    private MultipartFile file;

    /**
     * 父目录路径
     */
    private String parentPath;
}
