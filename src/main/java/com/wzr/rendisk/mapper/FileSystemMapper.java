package com.wzr.rendisk.mapper;

import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.FolderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件系统相关操作
 * @author wzr
 * @date 2023-06-07 10:09
 */
@Mapper
public interface FileSystemMapper {

    /**
     * 【创建目录】
     * @param folderInfo 目录
     * @return 响应行数
     */
    int insertFolder(FolderInfo folderInfo);
    
    /**
     * 根据虚拟路径查询目录id
     * @param virtualPath 虚拟路径
     * @return 目录id
     */
    Long getFolderIdByPath(@Param("userId") Long userId,
                           @Param("virtualPath") String virtualPath);

    /**
     * 获取当前目录下所有目录
     * @param userId 用户id
     * @param parentId 目录id
     * @return 指定目录下所有目录
     */
    List<FolderInfo> getFolderListById(Long userId, Long parentId);

    /**
     * 获取当前目录下所有文件
     * @param userId 用户id
     * @param parentId 目录id
     * @return 指定目录下所有文件
     */
    List<FileInfo> getFileListById(Long userId, Long parentId);

    /**
     * 获取指定路径下的文件info
     * @param userId 用户id
     * @param virtualPath 指定路径
     * @return 文件info
     */
    FileInfo getFileInfoByPath(Long userId, String virtualPath);
    
    /**
     * 上传文件
     * @param fileInfo 文件
     * @return 响应行数
     */
    int insertFile(FileInfo fileInfo);

    /**
     * 检查用户需要创建的目录是否存在
     * @param userId 用户id
     * @param virtualPath 全路径
     * @param type 类型，0-目录，1-文件
     * @return 查询到的记录
     */
    List<Object> queryVirtPathExist(Long userId, String virtualPath, Integer type);

    /**
     * 删除单个文件
     * @param userId
     * @param virtualPath
     * @return
     */
    int deleteFileByPath(@Param("userId") Long userId,
                         @Param("virtualPath") String virtualPath);

    /**
     * 删除所有子目录
     * @param userId
     * @param virtualPath
     * @return
     */
    int deleteChildFolder(@Param("userId") Long userId,
                          @Param("virtualPath") String virtualPath);

    /**
     * 删除所有子文件。
     * @param userId
     * @param virtualPath
     * @return
     */
    int deleteChildFile(Long userId, String virtualPath);
}