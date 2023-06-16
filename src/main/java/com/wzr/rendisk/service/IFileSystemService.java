package com.wzr.rendisk.service;

import com.wzr.rendisk.dto.BigFileAddDto;
import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FileListDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件系统相关操作
 * @author wzr
 * @date 2023-06-07 11:06
 */
public interface IFileSystemService {

    /**
     * 【创建目录】
     * @param folderAddDto 新增文件对象
     */
    void mkdir(User user, FolderAddDto folderAddDto);

    /**
     * 根据目录路径，获取该路径下所有目录和文件
     * @param userId 用户id
     * @param parentPath 前端请求的目录路径
     * @return 当前目录下，所有的目录和文件
     */
    FileListDto getListByFolderPath(Long userId, String parentPath);

    /**
     * 获取指定路径下的文件info
     * @param userId 用户id
     * @param virtualPath 指定路径
     * @return 文件info
     */
    FileInfo getFileInfoByPath(Long userId, String virtualPath);

    /**
     * 获取指定路径下的所有文件
     * @param userId
     * @param virtualPath
     * @return
     */
    List<FileInfo> getFileListByPath(Long userId, String virtualPath);

    /**
     * 上传文件。
     * @param user 用户
     * @param fileAddDto {MultipartFile, parentName}
     */
    void uploadFile(User user, FileAddDto fileAddDto);

    /**
     * 大文件分片上传
     * @param user 用户
     * @param fileAddDto 含文件file、分片索引sliceIndex、分片总数totalPieces、整体文件md5
     */
    String uploadBigFile(User user, BigFileAddDto fileAddDto);
    
    
    /**
     * 检查用户需要创建的目录/文件是否存在
     * @param userId 用户id
     * @param virtualPath 目录/文件的全路径
     * @param type 类型，0-目录，1-文件
     * @return true: 对应目录已存在。
     */
    boolean checkVirtPathExist(Long userId, String virtualPath, Integer type);

    /**
     * 获取对应路径下的文件流
     * @param username 用户名
     * @param filePath 文件路径
     * @return 文件流
     */
    InputStream getFileStream(String username, String filePath);

    /**
     * 删除对应路径下所有文件和目录
     * @param user 用户
     * @param virtualPath 对应路径
     * @param type 标记是目录和文件
     */
    void deleteByPath(User user, String virtualPath, Integer type);
}
