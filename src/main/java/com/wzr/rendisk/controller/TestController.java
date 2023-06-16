package com.wzr.rendisk.controller;

import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IAuthService;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.service.ISearchService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * @author wzr
 * @date 2023-06-01 23:58
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private MinioClientPlus minioClientPlus;
    @Autowired
    private IAuthService authService;
    
    /** 
     *  /test/jwt/** 需要jwt认证
     */
    @RequestMapping("/jwt/1")
    public ResultData<?> test() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        return GlobalResult.success(list);
    }

    @RequestMapping("/jwt/2")
    public ResultData<?> test2() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException();
        }
        return GlobalResult.success(list);
    }

    @RequestMapping("/jwt/3")
    public ResultData<?> test3() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return GlobalResult.success(list);
    }

    private static final String bucketName = "rendisk-wzr";
    
    @RequestMapping("/folder/exist")
    public ResultData<?> folderExist(String name) {
        // 是否存在
        boolean folderExist = minioClientPlus.isFolderExist(bucketName, name);
        return GlobalResult.success(folderExist);
    }

    @RequestMapping("/folder/create")
    public ResultData<?> test5(String name) throws Exception {
        // 创建目录
        ObjectWriteResponse result = minioClientPlus.createFolder(bucketName, name);
        return GlobalResult.success(result.versionId());
    }

    @RequestMapping("/file/remove")
    public ResultData<?> test6(String name) {
        // 删除目录
        try {
            minioClientPlus.removeFile(bucketName, name);
        } catch (Exception e) {
            throw new GlobalException();
        }
        return GlobalResult.success();
    }

    @RequestMapping("/folder/remove")
    public ResultData<?> test7(String name) {
        // 删除目录
        try {
            minioClientPlus.removeFolder(bucketName, name);
        } catch (Exception e) {
            throw new GlobalException();
        }
        return GlobalResult.success();
    }
    
    @Autowired
    private ISearchService searchService;

    @RequestMapping("/file/list")
    public ResultData<?> test8() {
        // 测试批量插入文档
        User user = authService.getUserByUsername("wzr");
        return GlobalResult.success(searchService.loadAllFile(user));
    }

    @RequestMapping("/file/delall")
    public ResultData<?> test9() {
        // 测试删除文档
        User user = authService.getUserByUsername("wzr");
        searchService.removeAllFile(user);
        return GlobalResult.success();
    }
    
    @Autowired
    private IFileSystemService fileSystemService;

    /**
     * 获取文件
     * @param user 用户
     * @param filePath 文件全路径
     * @return
     */
    @GetMapping("/file/get")
    public ResponseEntity<byte[]> getFile(User user, String filePath) throws IOException {
        // 尝试获取文件info
        FileInfo fileInfo = fileSystemService.getFileInfoByPath(user.getId(), filePath);
        if ( fileInfo == null ) {
            throw new GlobalException(ResultCode.ERROR);
        }
        // 获取文件流
        InputStream fileStream = fileSystemService.getFileStream(user.getUsername(), fileInfo.getRealPath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileInfo.getFileName(), "UTF-8"));
        return new ResponseEntity<byte[]>(IOUtils.toByteArray(fileStream), headers, HttpStatus.OK);
    }
}
