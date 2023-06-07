package com.wzr.rendisk.service;

import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FileListDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.User;

import java.io.InputStream;

/**
 * 文件系统相关操作
 * @author wzr
 * @date 2023-06-07 11:06
 */
public interface IFileSystemService {
    /* ================================ 文件相关操作 ================================ */
    /**
     * 上传文件
     * @param user
     * @param fileAddDto
     * @return
     */
    void uploadFile(User user, FileAddDto fileAddDto);

    /**
     * 获取文件
     * @param realPath
     * @return
     */
    InputStream getFile(String realPath);
    
    /* ================================ 目录相关操作 ================================ */
    /**
     * 【获取文件列表】
     * @param username 用户id
     * @param parentPath 父虚拟目录
     * @return 文件列表对象
     */
    FileListDto getList(String username, String parentPath);

    /**
     * 【创建目录】
     * @param folderAddDto 新增文件对象
     */
    void mkdir(FolderAddDto folderAddDto);

    /* ================================ 通用操作 ================================ */
    /**
     * 检查用户需要创建的目录是否存在
     * @param username
     * @param virtualPath
     * @param type 类型，0-目录，1-文件
     * @return
     */
    boolean checkVirtPathExist(String username, String virtualPath, Integer type);

    /**
     * 删除文件或目录（递归）
     * @param user
     * @param virtualPath
     * @param type
     * @return
     */
    void delete(User user, String virtualPath, Integer type);

    /**
     * 重命名文件或目录
     * @param user
     * @param newName
     * @param virtualPath
     * @param type
     * @return
     */
    void rename(User user, String newName, String virtualPath, Integer type);
}
