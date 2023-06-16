package com.wzr.rendisk.config.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取 Minio 相关配置文件
 * @author wzr
 * @date 2023-06-07 12:08
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "my-config.minio")
public class MinioProperties {

    /**
     * minio服务端地址
     */
    private String url;

    /**
     * minio认证用户名
     */
    private String accessKey;

    /**
     * minio认证密码
     */
    private String secretKey;

    /**
     * 需创建的桶名字
     */
    private String bucketNamePrefix;

    /**
     * 临时文件存储桶名
     */
    private String tempBucketName;
}
