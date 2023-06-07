package com.wzr.rendisk.controller;

import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultCode;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.service.IAuthService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author wzr
 * @date 2023-06-01 23:58
 */
@RestController
@RequestMapping("/api/v1")
public class TestController {
    
    @Autowired
    private MinioClientPlus minioClientPlus;
    @Autowired
    private IAuthService authService;
    
    @RequestMapping("/test")
    public ResultData<?> test() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        return GlobalResult.success(list);
    }

    @RequestMapping("/test2")
    public ResultData<?> test2() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException();
        }
        return GlobalResult.success(list);
    }

    @RequestMapping("/test3")
    public ResultData<?> test3() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return GlobalResult.success(list);
    }

    @RequestMapping("/test4")
    public ResultData<?> test4() {
        String bucketName = minioClientPlus.getBucketName("test");
        boolean folderExist = minioClientPlus.isFolderExist(bucketName, "/1/2/3");
        return GlobalResult.success(folderExist);
    }

    @RequestMapping("/test5")
    public ResultData<?> test5() throws Exception {
        String bucketName = minioClientPlus.getBucketName("test");
        ObjectWriteResponse result = minioClientPlus.createFolder(bucketName, "/my2/insight2");
        return GlobalResult.success(result.toString());
    }

    @RequestMapping("/test6")
    public ResultData<?> test6(){;
        return GlobalResult.success(authService.getCurrentUser(SecurityUtils.getSubject()));
    }
}
