package com.wzr.rendisk.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

/**
 * @author wzr
 * @date 2023-06-07 11:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FolderAddDto {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 父目录虚拟路径
     */
    private String parentPath;

    /**
     * 即将创建的名字
     */
    private String addName;

    /**
     * 即将创建的目录路径
     */
    @Nullable
    private String currVirtPath;
}
