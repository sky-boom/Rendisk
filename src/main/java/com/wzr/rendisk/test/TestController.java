package com.wzr.rendisk.test;

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
import org.springframework.web.bind.annotation.ResponseBody;
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
    private IFileSystemService fileSystemService;

    @RequestMapping("/file/url")
    public ResultData<?> getPresignedObjectUrl(String name) {
        FileInfo fileInfoByPath = fileSystemService.getFileInfoByPath(1000003L, name);
        String realPath = fileInfoByPath.getRealPath();
        // 获得文件外链
        try {
            return GlobalResult.success( minioClientPlus.getPresignedObjectUrl(bucketName, realPath, 60) );
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException();
        }
    }
    
    @Autowired
    private ISearchService searchService;

    @RequestMapping("/es/file/list")
    public ResultData<?> test8() {
        // 测试批量插入文档
        User user = authService.getUserByUsername("wzr");
        return GlobalResult.success(searchService.loadAllFile(user));
    }

    @RequestMapping("/es/file/delall")
    public ResultData<?> test9() {
        // 测试删除文档
        User user = authService.getUserByUsername("wzr");
        searchService.removeAllFile(user);
        return GlobalResult.success();
    }
    
    @RequestMapping("/es/search")
    @ResponseBody
    public ResponseEntity<?> testSearch(String keyword) {
        return ResponseEntity.ok( searchService.testSearch(keyword) );
    }

    @RequestMapping("/es/addone")
    @ResponseBody
    public ResponseEntity<?> testAddone() {
        return ResponseEntity.ok( searchService.testLoadDocument() );
    }

    
    @Autowired
    private TestMapper testMapper;
    
    @RequestMapping("/thumbsUp")
    @ResponseBody
    public ResponseEntity<?> testThumbsUp() {
        // 测试多用户点赞
        return ResponseEntity.ok( syncThumbsUp() );
    }
    
    private Integer syncThumbsUp() {
        synchronized (TestController.class) {
            return testMapper.testThumbsUp();
        }
    }

    private Integer thumbsUp() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testMapper.testThumbsUp();
    }
}
