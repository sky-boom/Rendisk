package com.wzr.rendisk.dto;

import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.FolderInfo;

import java.util.List;

/**
 * 返回文件列表对象
 * 包含：父目录id、目录列表、文件列表
 * @author wzr
 * @date 2023-06-07 11:10
 */
public class FileListDto {
    /**
     * 父目录
     */
    private String parentPath;

    /**
     * 面包屑对象列表
     * 如：当前目录是 /test/mydir/haha
     * 则列表 = path = ["/test", "/test/mydir", "/test/mydir/haha"]
     *         name = ["test",        "mydir"              "haha"]
     */
    private List<FileBreadcrumbDto> breadcrumbList;

    /**
     * 目录列表
     */
    private List<FolderInfo> folderList;

    /**
     * 文件列表
     */
    private List<FileInfo> fileList;
}
