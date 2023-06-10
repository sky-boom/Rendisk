package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.config.minio.ContentType;
import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.config.minio.MinioProperties;
import com.wzr.rendisk.core.constant.FileSysConstant;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.dto.BreadcrumbDto;
import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FileListDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.FolderInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.FileSystemMapper;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.utils.DBUtils;
import com.wzr.rendisk.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    private MinioProperties minioProperties;
    @Autowired
    private MinioClientPlus minioClientPlus;
    
    /**
     * 分隔符
     */
    private static final String DIVIDE = "/";
    

    @Override
    public void mkdir(User user, FolderAddDto folderAddDto) {
        // 1.先向数据库中插入数据，再在服务器中创建目录
        FolderInfo folderInfo = new FolderInfo();
        // 设置父目录id
        String parentPath = folderAddDto.getParentPath();
        if (StringUtils.isEmpty( parentPath )) {
            parentPath = "";
            folderInfo.setParentId(-1L);
        } else {
            folderInfo.setParentId( fileSystemMapper.getFolderIdByPath(user.getId(), parentPath) );
        }
        // 走到这里，addName 必不为null
        String addName = folderAddDto.getAddName();
        // 虚拟路径:    /haha/test 或 "/test"   真实路径:    /2021/12/27/haha/test
        String virtualPath = generateVirtPath(parentPath, addName);
        String realPath = generateRealPath(virtualPath);
        folderInfo.setVirtualPath(virtualPath);
        folderInfo.setRealPath(realPath);
        folderInfo.setUserId(user.getId());
        folderInfo.setFolderName(addName);
        DBUtils.checkOperation( fileSystemMapper.insertFolder(folderInfo) );
        
        // 2.数据库插入成功，然后插入至存储介质
        try {
            minioClientPlus.createFolder(
                    minioClientPlus.getBucketByUsername(user.getUsername()), 
                    realPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }

    @Override
    public FileListDto getListByFolderPath(Long userId, String parentPath) {
        // 根据用户的虚拟路径获取对应目录id
        Long parentId;
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "";
            parentId = -1L;
        } else {
            parentId = fileSystemMapper.getFolderIdByPath(userId, parentPath);
        }
        // 获取目录列表
        List<FolderInfo> folderList = fileSystemMapper.getFolderListById(userId, parentId);
        // 获取文件列表
        List<FileInfo> fileList = fileSystemMapper.getFileListById(userId, parentId);
        // 由parentPath处理面包屑显示列表
        List<BreadcrumbDto> breadcrumbs = new ArrayList<>();
        String[] names = parentPath.split("/");
        StringBuilder eachPath = new StringBuilder();
        for ( int i = 1; i < names.length; i ++ ) {
            eachPath.append("/").append(names[i]);
            breadcrumbs.add(new BreadcrumbDto(names[i], eachPath.toString()));
        }
        // 填充数据
        FileListDto fileListDto = new FileListDto();
        fileListDto.setParentPath(parentPath);
        fileListDto.setFolderList(folderList);
        fileListDto.setFileList(fileList);
        fileListDto.setBreadcrumbList(breadcrumbs);

        return fileListDto;
    }

    @Override
    public FileInfo getFileInfoByPath(Long userId, String virtualPath) {
        // 正常情况下，该sql至多查询出一条数据
        return fileSystemMapper.getFileInfoByPath(userId, virtualPath);
    }

    @Override
    public List<FileInfo> getFileListByPath(Long userId, String virtualPath) {
        return fileSystemMapper.getFileListByPath(userId, virtualPath);
    }

    @Override
    public void uploadFile(User user, FileAddDto fileAddDto) {
        // 设置父目录id
        FileInfo fileInfo = new FileInfo();
        String parentPath = fileAddDto.getParentPath();
        if (StringUtils.isEmpty( parentPath )) {
            fileInfo.setParentId(-1L);
            parentPath = "";
        } else {
            fileInfo.setParentId(fileSystemMapper.getFolderIdByPath(user.getId(), parentPath));
        }
        // 走到这里，addName 必不为null
        String addName = fileAddDto.getFile().getOriginalFilename();
        // 虚拟路径:    /haha/test 或 "/test"   真实路径:    /2021/12/27/haha/test
        String virtualPath = generateVirtPath(parentPath, addName);
        String realPath = generateRealPath(virtualPath);
        fileInfo.setVirtualPath(virtualPath);
        fileInfo.setRealPath(realPath);
        fileInfo.setUserId(user.getId());
        fileInfo.setFileName(addName);
        fileInfo.setFileSize(fileAddDto.getFile().getSize());
        DBUtils.checkOperation( fileSystemMapper.insertFile(fileInfo) );
        
        // 2.数据库插入成功，再上传至存储介质
        try {
            // 注意，上传的名字应该是完整的路径 realPath
            minioClientPlus.uploadFile(
                    minioClientPlus.getBucketByUsername(user.getUsername()), 
                    fileAddDto.getFile(), 
                    realPath, ContentType.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }
    
    @Override
    public boolean checkVirtPathExist(Long userId, String virtualPath, Integer type) {
        List<Object> folderInfo = fileSystemMapper.queryVirtPathExist(userId, virtualPath, type);
        if (folderInfo.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public InputStream getFileStream(String username, String filePath) {
        try {
            return minioClientPlus.getFileStream(
                    minioClientPlus.getBucketByUsername(username), 
                    generateRealPath(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }

    @Override
    public void deleteByPath(User user, String virtualPath, Integer type) {
        String realPath = null;
        // 如果是文件，直接删除即可
        if (FileSysConstant.FILE_TYPE.equals(type)) {
            // 删除当前文件
            DBUtils.checkOperation( fileSystemMapper.deleteFileByPath(user.getId(), virtualPath) );
        }
        // 如果是目录，使用通配符删除该路径下的所有目录和文件
        if (FileSysConstant.FOLDER_TYPE.equals(type)) {
            // 删除所有子目录
            fileSystemMapper.deleteChildFolder(user.getId(), virtualPath);
            // 删除所有子文件
            fileSystemMapper.deleteChildFile(user.getId(), virtualPath);
        }
        // 删除完毕，开始删除实际存储的内容
//        try {
//            minioClientPlus.removeFile();
//        }
    }

    /**
     * 前端传来父目录+新增的目录名（或新增的文件名），这里将其合并成一个完整的路径。
     * @param parentPath 父目录 "/test"
     * @param addName 新增的目录名（或新增的文件名） "hello.txt"
     * @return 完整的路径 "/test/hello.txt"
     */
    private static String generateVirtPath(String parentPath, String addName) {
        return parentPath + DIVIDE + addName;
    }
    
    /**
     * 通过虚拟路径，构造真实的存储路径
     * @param virtualPath 虚拟路径 "/test/hi"
     * @return 真实路径 "/2023/6/7/{virtualPath}"
     */
    private static String generateRealPath(String virtualPath) {
        return DIVIDE + DateUtils.getCurrFormatDateStr("yyyy/MM/dd") + virtualPath;
    }
    
}
