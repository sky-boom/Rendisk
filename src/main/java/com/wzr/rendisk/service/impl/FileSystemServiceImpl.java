package com.wzr.rendisk.service.impl;

import com.wzr.minio.client.FragResult;
import com.wzr.minio.client.MinioUtils;
import com.wzr.minio.config.ContentType;
import com.wzr.rendisk.core.constant.FileSysConstant;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.dto.*;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.FolderInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.mapper.FileSystemMapper;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.utils.DBUtils;
import com.wzr.rendisk.utils.DateUtils;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wzr
 * @date 2023-06-07 11:13
 */
@Slf4j
@Transactional(rollbackFor=Exception.class)
@Service
public class FileSystemServiceImpl implements IFileSystemService {
    
    @Autowired
    private FileSystemMapper fileSystemMapper;
    @Autowired
    private MinioUtils minioUtils;
    @Autowired
    private ThreadPoolExecutor commonThreadPool;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Value("${minio-util.bucket-name-prefix}")
    private String bucketNamePrefix;
    
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
            minioUtils.createFolder(
                    getBucketByUsername(user.getUsername()), 
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
        // 文件信息插入数据库
        FileInfo fileInfo = getFileInfo(fileAddDto, user.getId());
        DBUtils.checkOperation( fileSystemMapper.insertFile(fileInfo) );
        // 数据库插入成功，再上传至存储介质
        try {
            // 注意，上传的名字应该是完整的路径 realPath
            minioUtils.uploadFile(
                    getBucketByUsername(user.getUsername()), 
                    fileAddDto.getFile(),
                    fileInfo.getRealPath(), ContentType.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }

    @Override
    public String uploadBigFile(User user, BigFileAddDto fileAddDto) {
        try {
            MultipartFile file = fileAddDto.getFile();
            Integer currIndex = fileAddDto.getCurrIndex();
            Integer totalPieces = fileAddDto.getTotalPieces();
            String md5 = fileAddDto.getMd5();
            log.info("[Bigfile] 上传文件md5: {} ,分片索引: {}", md5, currIndex);
            FragResult fragResult = minioUtils.uploadFileFragment(file, currIndex, totalPieces, md5);
            // 分片全部上传完毕
            if ( fragResult.isAllCompleted() ) {
                FileInfo fileInfo = getFileInfo(fileAddDto, user.getId());
                DBUtils.checkOperation( fileSystemMapper.insertFile(fileInfo) );
                String realPath = generateRealPath(generateVirtPath(fileAddDto.getParentPath(), file.getOriginalFilename()));
                // 发起文件合并请求, 无异常则成功
                minioUtils.composeFileFragment(getBucketByUsername(user.getUsername()), realPath, totalPieces, md5);
                return "-1";
            } else {
                Iterator<Integer> iterator = fragResult.getRemainIndex().iterator();
                if (iterator.hasNext()) {
                    String nextIndex = iterator.next().toString();
                    log.info("[BigFile] 下一个需上传的文件索引是:{}", nextIndex);
                    return nextIndex;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.error("[Bigfile] 上传文件时出现异常");
        throw new GlobalException(ResultCode.FILE_UPLOAD_ERROR);
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
        InputStream inputStream;
        inputStream = singleThreadGet(username, filePath);
        return inputStream;
    }
    
    /** 单线程获取文件（效率还可以） */
    private InputStream singleThreadGet(String username, String filePath) {
        try {
            return minioUtils.getFileStream(
                    getBucketByUsername(username),
                    filePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResultCode.ERROR);
        }
    }
    
    /* 多线程获取文件（效率低，不知何故） */
    /*
    private InputStream multiThreadGet(String username, String filePath){
        final String bucketName = getBucketByUsername(username);
        StatObjectResponse info = minioUtils.getFileStatusInfo(bucketName, filePath);
          //如果是105字节的文件，10个线程执行，平均分配下，有：
          //     ceil (105 / 10d) = 11.0, 但10个线程总共会操作110字节
          //     因此，可以让前9个线程负责11字节(共99字节)，最后1个线程按需分配字节(需执行6字节)
        int corePoolSize = commonThreadPool.getCorePoolSize();
        long size = info.size();
        long sliceLength = (long) Math.ceil( size / (double) corePoolSize );
        log.info("得到文件 {} 的长度: {}", filePath, size);
        // 根据核心线程数，来分配文件下载任务
        long offset = 0;
        List<Future<InputStream>> futureList = new ArrayList<>();
        for (int i = 0; i < corePoolSize; i++) {
            long currLength = sliceLength;
            // 如果超过文件容量，说明文件到达最后一段，处理这一段后，下一步必定会退出循环。
            if (offset + sliceLength >= size) {
                currLength = size - offset;
            }
            // 使用隐式final变量，让lambda知道这两个变量不会再赋其他值
            long fixedOffset = offset;
            long fixedLength = currLength;
            Future<InputStream> future = CompletableFuture.supplyAsync(() -> {
                log.info("[下载文件] {}正在下载字节范围在 [{}, {}] 之间的文件...",
                        Thread.currentThread().getName(), fixedOffset, fixedOffset + fixedLength - 1);
                return minioUtils.getFileStream(bucketName, filePath, fixedOffset, fixedLength);
            }, commonThreadPool);
            futureList.add(future);
            offset += sliceLength;
        }
        // 处理线程返回结果，如果每个线程执行耗时不同，则会等到所有线程执行完毕再处理
        long startTime = System.currentTimeMillis();
        List<InputStream> inputStreams = futureList.stream().map(futureItem -> {
            try {
                return futureItem.get();
            } catch (Exception e) {
                e.printStackTrace();
                throw new GlobalException(ResultCode.ERROR);
            }
        }).collect(Collectors.toList());
        log.info("把future转化为inputStream，共耗时: {} ms", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        InputStream sequenceInputStream = new SequenceInputStream(Collections.enumeration(inputStreams));
        log.info("合并inputStream列表，共耗时: {} ms", System.currentTimeMillis() - startTime);
        return sequenceInputStream;
    }*/

    @Override
    public void deleteByPath(User user, String virtualPath, Integer type) {
        // 先查询数据库，再删掉
        FileInfo fileInfo = fileSystemMapper.getFileInfoByPath(user.getId(), virtualPath);
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
        // 删除完毕，开始删除实际存储的内容（未完成）
        try {
            minioUtils.removeFile(getBucketByUsername(
                    user.getUsername()), fileInfo.getRealPath());
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException();
        }
    }

    /**
     * 根据前端传来的数据，返回能够插入数据库的文件对象
     * @param fileAddDto dto
     * @param userId 用户id
     * @return FileInfo
     */
    private FileInfo getFileInfo(FileAddDto fileAddDto, Long userId) {
        // 设置父目录id
        FileInfo fileInfo = new FileInfo();
        String parentPath = fileAddDto.getParentPath();
        if (StringUtils.isEmpty( parentPath )) {
            fileInfo.setParentId(-1L);
            parentPath = "";
        } else {
            fileInfo.setParentId(fileSystemMapper.getFolderIdByPath(userId, parentPath));
        }
        // 走到这里，addName 必不为null
        String addName = fileAddDto.getFile().getOriginalFilename();
        // 虚拟路径:    /haha/test 或 "/test"   真实路径:    /2021/12/27/haha/test
        String virtualPath = generateVirtPath(parentPath, addName);
        String realPath = generateRealPath(virtualPath);
        fileInfo.setVirtualPath(virtualPath);
        fileInfo.setRealPath(realPath);
        fileInfo.setUserId(userId);
        fileInfo.setFileName(addName);
        fileInfo.setFileSize(fileAddDto.getFile().getSize());
        
        return fileInfo;
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
    
    /**
     * 通过用户名获取Minio的桶名
     * @param username 用户名
     * @return 桶名
     */
    private String getBucketByUsername(String username) {
        return bucketNamePrefix + username;
    }
}
