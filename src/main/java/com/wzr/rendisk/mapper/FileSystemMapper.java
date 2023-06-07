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
    /*===================== 文件相关操作 ======================*/
    /**
     * 获取【文件列表】
     * @param userId
     * @param parentId
     * @return
     */
    List<FileInfo> queryFileListById(@Param("userId") Long userId,
                                     @Param("parentId") Long parentId);

    /**
     * 查询【单个文件】
     * @param userId
     * @param virtualPath
     * @return
     */
    FileInfo queryFileByPath(@Param("userId") Long userId,
                             @Param("virtualPath") String virtualPath);

    /**
     * 删除单个文件
     * @param userId
     * @param virtualPath
     * @return
     */
    int deleteFileByPath(@Param("userId") Long userId,
                         @Param("virtualPath") String virtualPath);

    /**
     * 【上传文件】到MySQL
     */
    int addFile(FileInfo fileInfo);

    /**
     * 【重命名文件】
     * @param newInfo 重命名后的文件对象
     * @param oldInfo 重命名前的文件对象
     * @return
     */
    int renameFile(@Param("oldInfo") FileInfo oldInfo,
                   @Param("newInfo") FileInfo newInfo);

    /*===================== 目录相关操作 ======================*/

    /**
     * 获取【目录列表】
     * @param userId
     * @param parentId
     * @return
     */
    List<FolderInfo> queryFolderListById(@Param("userId") Long userId,
                                         @Param("parentId") Long parentId);

    /**
     * 查询【单个目录】
     * @param userId
     * @param virtualPath
     * @return
     */
    FolderInfo queryFolderByPath(@Param("userId") Long userId,
                                 @Param("virtualPath") String virtualPath);

    /**
     * 【创建目录】
     * @param folderInfo
     * @return
     */
    int addFolder(FolderInfo folderInfo);

    /**
     * 【删除所有子目录】
     * @param userId
     * @param virtualPath
     * @return
     */
    int deleteFolder(@Param("userId") Long userId,
                     @Param("virtualPath") String virtualPath);

    /**
     * 【重命名当前目录】及更新所有子目录路径
     * @return
     */
    int renameFolder(@Param("oldInfo") FolderInfo oldInfo,
                     @Param("newInfo") FolderInfo newInfo);

    /*================================ 其他通用查询 =================================*/

    /**
     * 检查用户需要创建的目录是否存在
     * @param userId
     * @param virtualPath
     * @param type 0-目录，1-文件
     * @return
     */
    List<Object> queryVirtPathExist(@Param("userId") Long userId,
                                    @Param("virtualPath") String virtualPath,
                                    @Param("type") Integer type);

    /**
     * 根据id查询虚拟路径
     * @param folderId
     * @return
     */
    String queryFolderPathById(@Param("userId") Long userId,
                               @Param("folderId") Long folderId);

    /**
     * 根据虚拟路径查询id
     * @param virtualPath
     * @return
     */
    Long queryFolderIdByPath(@Param("userId") Long userId,
                             @Param("virtualPath") String virtualPath);
}