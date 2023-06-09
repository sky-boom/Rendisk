package com.wzr.rendisk.controller;

import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.core.constant.FileSysConstant;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.dto.FileAddDto;
import com.wzr.rendisk.dto.FolderAddDto;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IFileSystemService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件系统接口
 * @author wzr
 * @date 2023-06-07 20:25
 */
@RestController
@RequestMapping("/api/v1/fs")
public class FileSystemController {
    
    @Autowired
    private IFileSystemService fileSystemService;
    @Autowired
    private MinioClientPlus minioClientPlus;
    
    /**
     * 【创建目录】
     * @param user 当前登录的用户(已配置mvc参数)
     * @param folderAddDto (parentPath, addName)前端文件夹对象
     * @return success()
     */
    @RequestMapping("/folder/mkdir")
    public ResultData<?> mkdir(User user, FolderAddDto folderAddDto) {
        // 名字为空，或包含特殊字符，则提示错误
        if (folderAddDto.getAddName() == null || folderAddDto.getAddName().matches(FileSysConstant.NAME_EXCEPT_SYMBOL)) {
            throw new GlobalException(ResultCode.INCORRECT_FILE_NAME);
        }
        fileSystemService.mkdir(user, folderAddDto);
        // 无异常时，即返回成功。
        return GlobalResult.success();
    }

    /**
     * 查询目录下【所有目录和文件】
     * @param user 当前登录的用户(已配置mvc参数)
     * @param parentPath 需要查询的目录
     * @return
     */
    @RequestMapping("/folder/list")
    public ResultData<?> getFileList(User user, String parentPath) {
        return GlobalResult.success(fileSystemService.getListByFolderPath(user.getId(), parentPath));
    }

    /**
     * 上传文件到指定“目录”
     * @param user 当前登录的用户(已配置mvc参数)
     * @param fileAddDto 包含文件、父目录路径
     * @return
     */
    @RequestMapping("/file/upload")
    public ResultData<?> getFileList(User user, FileAddDto fileAddDto) {
        // 1.文件为空，返回失败 (一般不是用户的问题)
        if (fileAddDto.getFile() == null) {
            return GlobalResult.error();
        }
        // 2.名字为空，或包含特殊字符，则提示错误
        String fileName = fileAddDto.getFile().getOriginalFilename();
        if (StringUtils.isEmpty(fileName) || fileName.matches(FileSysConstant.NAME_EXCEPT_SYMBOL)) {
            throw new GlobalException(ResultCode.INCORRECT_FILE_NAME);
        }
        fileSystemService.uploadFile(user, fileAddDto);
        // 无异常时，即返回成功。
        return GlobalResult.success();
    }

    /**
     * 获取文件
     * @param user 用户
     * @param filePath 文件全路径
     * @return
     */
    @RequestMapping("/file/get")
    public ResponseEntity<byte[]> getFile(User user, String filePath) throws IOException {
        // 尝试获取文件info
        FileInfo fileInfo = fileSystemService.getFileInfoByPath(user.getId(), filePath);
        if ( fileInfo == null ) {
            throw new GlobalException(ResultCode.ERROR);
        }
        // 获取文件流
        InputStream fileStream = fileSystemService.getFileStream(user.getUsername(), filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileInfo.getFileName(), "UTF-8"));
        return new ResponseEntity<byte[]>(IOUtils.toByteArray(fileStream), headers, HttpStatus.OK);
    }

    /**
     * 删除文件或目录
     * @param user 用户 
     * @param virtualPath 要删除的路径
     * @param type 0-目录 1-文件
     * @return
     */
    @RequestMapping("/delete")
    public ResultData<?> delete(User user, String virtualPath, Integer type) {
        // 判断所选文件或目录是否为空
        if (StringUtils.isEmpty(virtualPath)) {
            throw new GlobalException(ResultCode.INCORRECT_FILE_NAME);
        }
        // 开始删除数据库及HDFS的数据
        fileSystemService.deleteByPath(user, virtualPath, type);
        // 无异常时，即返回成功。
        return GlobalResult.success();
    }
    
}
