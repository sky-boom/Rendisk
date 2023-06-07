package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FileListDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.FolderInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.FileSystemMapper;
import com.wzr.rendisk.service.IFileSystemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * @author wzr
 * @date 2023-06-07 11:13
 */
@Transactional(rollbackFor=Exception.class)
@Service
public class FileSystemServiceImpl implements IFileSystemService {
    
    @Autowired
    private FileSystemMapper fileSystemMapper;
    
    @Override
    public void uploadFile(User user, FileAddDto fileAddDto) {
//        // 1.先向数据库中插入数据
//        FileInfo fileInfo = new FileInfo();
//
//        // 设置父目录id
//        if (StringUtils.isEmpty( fileAddDto.getParentPath() )) {
//            fileInfo.setParentId(-1L);
//        } else {
//            fileInfo.setParentId( fileSystemMapper.queryFolderIdByPath(user.getId(), fileAddDto.getParentPath()) );
//        }
//
//        // 设置即将添加的文件信息
//        fileInfo.setUserId(user.getId());
//        fileInfo.setFileName(fileAddDto.getFile().getOriginalFilename());
//        // 虚拟路径:    /haha/test.txt 或 "/test.txt"
//        String virtualPath = fileAddDto.getCurrVirtPath();
//        fileInfo.setVirtualPath(virtualPath);
//        // 真实路径:    /hdfs-cloudisk/2021/12/27/haha/test
//        fileInfo.setRealPath(Cloudisk.generalPathPrefix(user.getId()) + virtualPath);
//        // 文件大小
//        fileInfo.setFileSize(fileAddDto.getFile().getSize());
//        // 插入数据
//        int affectRows = fileSystemMapper.addFile(fileInfo);
//        if (affectRows == 0) {
//            throw new GlobalException(RespBeanEnum.HDFS_UPLOAD_ERROR);
//        }
//        // 更新用户容量
//        userService.updateOneFileSize(user.getId(), fileInfo.getFileSize(), Cloudisk.USER_SIZE_INCREASE);
//        // 2.数据库插入成功，再上传至HDFS集群
//        InputStream fileStream;
//        try {
//            fileStream = fileAddDto.getFile().getInputStream();
//        } catch (Exception e) {
//            throw new GlobalException();
//        }
//        boolean result = hdfsService.upload(fileInfo.getRealPath(), fileStream);
//        if (!result) {
//            throw new GlobalException(RespBeanEnum.HDFS_UPLOAD_ERROR);
//        }
    }

    @Override
    public InputStream getFile(String realPath) {
        return null;
    }

    @Override
    public FileListDto getList(Long userId, String parentPath) {
        return null;
    }

    @Override
    public void mkdir(Long userId, FolderAddDto folderAddDto) {
//        // 1.先向数据库中插入数据，再向HDFS创建文件
//        FolderInfo folderInfo = new FolderInfo();
//        // 设置父目录id
//        if (StringUtils.isEmpty( folderAddDto.getParentPath() )) {
//            folderInfo.setParentId(-1L);
//        } else {
//            folderInfo.setParentId( fileSystemMapper.queryFolderIdByPath(userId, folderAddDto.getParentPath()) );
//        }
//        // 设置即将添加的文件信息
//        folderInfo.setUserId(userId);
//        folderInfo.setFolderName(folderAddDto.getAddName());
//        // 虚拟路径:    /haha/test 或 "/test"
//        String virtualPath = folderAddDto.getCurrVirtPath();
//        folderInfo.setVirtualPath(virtualPath);
//        // 真实路径:    /hdfs-cloudisk/2021/12/27/haha/test
//        folderInfo.setRealPath(Cloudisk.generalPathPrefix(userId) + virtualPath);
//        // 插入数据
//        int affectRows = fileSystemMapper.addFolder(folderInfo);
//        if (affectRows == 0) {
//            throw new GlobalException(RespBeanEnum.CLOUD_FOLDER_RENAME_ERR);
//        }
//        // 2.数据库插入成功，然后插入HDFS集群
//        boolean result = hdfsService.mkdirs(folderInfo.getRealPath());
//        if (!result) {
//            throw new GlobalException(RespBeanEnum.HDFS_MKDIR_ERROR);
//        }
    }

    @Override
    public boolean checkVirtPathExist(Long userId, String virtualPath, Integer type) {
        return false;
    }

    @Override
    public void delete(User user, String virtualPath, Integer type) {

    }

    @Override
    public void rename(User user, String newName, String virtualPath, Integer type) {

    }
}
