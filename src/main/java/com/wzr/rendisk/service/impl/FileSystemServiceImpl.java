package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.config.minio.MinioProperties;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FileListDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.FolderInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.FileSystemMapper;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.utils.DateUtils;
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
    @Autowired
    private IAuthService authService;
    @Autowired
    private MinioProperties minioProperties;
    @Autowired
    private MinioClientPlus minioClientPlus;
    
    /**
     * 分隔符
     */
    private static final String DIVIDE = "/";
    
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
    public FileListDto getList(String username, String parentPath) {
        return null;
    }

    @Override
    public void mkdir(FolderAddDto folderAddDto) {
        User user = authService.getUserByUsername(folderAddDto.getUsername());
        if (user == null) {
            throw new GlobalException(ResultCode.USER_EMPTY);
        }
        // 1.先向数据库中插入数据，再在服务器中创建目录
        FolderInfo folderInfo = new FolderInfo();
        // 设置父目录id
        if (StringUtils.isEmpty( folderAddDto.getParentPath() )) {
            folderInfo.setParentId(-1L);
        } else {
            folderInfo.setParentId( fileSystemMapper.queryFolderIdByPath(user.getId(), folderAddDto.getParentPath()) );
        }
        // 设置即将添加的文件信息
        folderInfo.setUserId(user.getId());
        folderInfo.setFolderName(folderAddDto.getAddName());
        // 虚拟路径:    /haha/test 或 "/test"
        String virtualPath = folderAddDto.getCurrVirtPath();
        folderInfo.setVirtualPath(virtualPath);
        // 真实路径:    /2021/12/27/haha/test
        String realPath = generateRealPath(virtualPath);
        folderInfo.setRealPath(realPath);
        // 插入数据
        fileSystemMapper.addFolder(folderInfo);
        // 2.数据库插入成功，然后插入至存储介质
        try {
            minioClientPlus.createFolder(getBucketByUsername(user.getUsername()), realPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }

    @Override
    public boolean checkVirtPathExist(String username, String virtualPath, Integer type) {
        return false;
    }

    @Override
    public void delete(User user, String virtualPath, Integer type) {

    }

    @Override
    public void rename(User user, String newName, String virtualPath, Integer type) {

    }

    /**
     * 通过用户名获取Minio的桶名
     * @param username 用户名
     * @return 桶名
     */
    private String getBucketByUsername(String username) {
        return minioProperties.getBucketNamePrefix() + username;
    }
            
    /**
     * 通过虚拟路径，构造真实的存储路径
     * @param virtualPath 虚拟路径 /test/hi
     * @return 真实路径 /2023/6/7/{virtualPath}
     */
    private static String generateRealPath(String virtualPath) {
        return DIVIDE + DateUtils.getCurrFormatDateStr("yyyy/MM/dd") + virtualPath;
    }
    
}
