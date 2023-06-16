package com.wzr.rendisk.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 继承自:
 *     private MultipartFile file;
 *     private String parentPath;
 * @author wzr
 * @date 2023-06-15 22:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BigFileAddDto extends FileAddDto {

    /**
     * 分片索引
     */
    private Integer sliceIndex;

    /**
     * 切片总数
     */
    private Integer totalPieces;

    /**
     * 文件md5
     */
    private String md5;
}
