package com.wzr.rendisk.config.minio;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 封装java-minio的操作
 * 参考：https://blog.csdn.net/qq_45774645/article/details/124252813
 * 
 * @author wzr
 * @date 2023-06-07 15:48
 */
@Component
@Slf4j
public class MinioClientPlus {

    @Autowired
    private MinioProperties minioProperties;
    
    @Autowired
    private MinioClient minioClient;

    @Bean
    public MinioClient getMinioClient() {
        log.debug("[Minio] 初始化 MinioClient...");
        log.debug("[Minio] 加载服务器: {}", minioProperties.getUrl());
        return MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /* *****************************  Operate Bucket Start  ******************************/

    /**
     * 判断Bucket是否存在
     * @return true：存在，false：不存在
     */
    private boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }
    
    /**
     * 如果一个桶不存在，则创建该桶
     */
    public void initialBucket(String bucketName) throws Exception {
        if (!bucketExists( bucketName )) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 获取 Bucket 的相关信息
     */
    public Optional<Bucket> getBucketInfo(String bucketName) throws Exception {
        return minioClient.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /* *****************************  Operate Bucket End  ******************************/

    /* *****************************  Operate Files Start  ******************************/

    /**
     * 使用MultipartFile进行文件上传
     * @param bucketName 存储桶
     * @param file 文件
     * @param fileName 对象名
     * @param contentType 类型
     * @return
     * @throws Exception
     */
    public ObjectWriteResponse uploadFile(String bucketName, MultipartFile file,
                                          String fileName, ContentType contentType) throws Exception {
        InputStream inputStream = file.getInputStream();
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .contentType(contentType.getValue())
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
    }

    /**
     * 上传本地文件
     * @param bucketName 存储桶
     * @param fileName 文件名称
     * @param filePath 本地文件路径
     */
    public ObjectWriteResponse uploadFile(String bucketName, String fileName,
                                          String filePath) throws Exception {
        return minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .filename(filePath)
                        .build());
    }

    /**
     * 通过流上传文件
     *
     * @param bucketName 存储桶
     * @param fileName 文件名
     * @param inputStream 文件流
     */
    public ObjectWriteResponse uploadFile(String bucketName, String fileName, InputStream inputStream) throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
    }
    
    /**
     * 判断文件是否存在
     * @param bucketName 存储桶
     * @param fileName 文件名
     * @return true: 存在
     */
    public boolean isFileExist(String bucketName, String fileName) {
        boolean exist = true;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 判断文件夹是否存在
     * @param bucketName 存储桶
     * @param folderName 文件夹名称
     * @return true: 存在
     */
    public boolean isFolderExist(String bucketName, String folderName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(folderName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && folderName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 创建文件夹或目录
     * @param bucketName 存储桶
     * @param folderName 目录路径
     */
    public ObjectWriteResponse createFolder(String bucketName, String folderName) throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(folderName)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    /**
     * 获取文件信息, 如果抛出异常则说明文件不存在
     *
     * @param bucketName 存储桶
     * @param fileName 文件名称
     */
    public String getFileStatusInfo(String bucketName, String fileName) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()).toString();
    }

    /**
     * 根据文件前缀查询文件
     * @param bucketName 存储桶
     * @param prefix 前缀
     * @param recursive 是否使用递归查询
     * @return MinioItem 列表
     */
    public List<Item> getAllFilesByPrefix(String bucketName,
                                            String prefix,
                                            boolean recursive) throws Exception {
        List<Item> list = new ArrayList<>();
        Iterable<Result<Item>> objectsIterator = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build());
        if (objectsIterator != null) {
            for (Result<Item> o : objectsIterator) {
                Item item = o.get();
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 获取文件的二进制流
     * @param bucketName 存储桶
     * @param fileName 文件名
     * @return 二进制流
     */
    public InputStream getFileStream(String bucketName, String fileName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
    }

    /**
     * 断点下载
     * @param bucketName 存储桶
     * @param fileName 文件名称
     * @param offset 起始字节的位置
     * @param length 要读取的长度
     * @return 二进制流
     */
    public InputStream getFileStream(String bucketName, String fileName, long offset, long length) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .offset(offset)
                        .length(length)
                        .build());
    }

    /**
     * 获取路径下文件列表
     * @param bucketName 存储桶
     * @param prefix 文件名称
     * @param recursive 是否递归查找，false：模拟文件夹结构查找
     * @return 二进制流
     */
    public Iterable<Result<Item>> getFilesByPrefix(String bucketName, String prefix,
                                                     boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(recursive)
                        .build());
    }

    /**
     * 拷贝文件
     *
     * @param bucketName 存储桶
     * @param fileName 文件名
     * @param srcBucketName 目标存储桶
     * @param srcFileName 目标文件名
     */
    public ObjectWriteResponse copyFile(String bucketName, String fileName,
                                               String srcBucketName, String srcFileName) throws Exception {
        return minioClient.copyObject(
                CopyObjectArgs.builder()
                        .source(CopySource.builder().bucket(bucketName).object(fileName).build())
                        .bucket(srcBucketName)
                        .object(srcFileName)
                        .build());
    }

    /**
     * 删除文件
     * @param bucketName 存储桶
     * @param fileName 文件名称
     */
    public void removeFile(String bucketName, String fileName) throws Exception {
         minioClient.removeObject(
                 RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());
    }

    /**
     * 批量删除文件
     * @param bucketName 存储桶
     * @param keys 需要删除的文件列表
     * @return
     */
    public void removeFiles(String bucketName, List<String> keys) {
        List<DeleteObject> objects = new LinkedList<>();
        keys.forEach(s -> {
            objects.add(new DeleteObject(s));
            try {
                this.removeFile(bucketName, s);
            } catch (Exception e) {
                log.error("批量删除失败！error:{}",e.getMessage());
            }
        });
    }

    /**
     * 获取文件外链
     * @param bucketName 存储桶
     * @param fileName 文件名
     * @param expires 过期时间 <=7 秒 （外链有效时间（单位：秒））
     * @return url
     * @throws Exception
     */
    public String getPresignedObjectUrl(String bucketName, String fileName, Integer expires) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder().expiry(expires).bucket(bucketName).object(fileName).build();
        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * 获得文件外链
     * @param bucketName
     * @param fileName
     * @return url
     * @throws Exception
     */
    public String getPresignedObjectUrl(String bucketName, String fileName) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * 将URLDecoder编码转成UTF8
     * @param str
     * @return
     */
    public static String getUtf8ByURLDecoder(String str) throws UnsupportedEncodingException {
        String url = str.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        return URLDecoder.decode(url, "UTF-8");
    }

    /* *****************************  Operate Files End  ******************************/

    /**
     * 根据用户名获取桶名
     * @param username
     * @return
     */
    public String getBucketName(String username) {
        return minioProperties.getBucketNamePrefix() + username;
    }
}
