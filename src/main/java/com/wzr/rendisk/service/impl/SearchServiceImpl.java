package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.config.es.ElasticSearchClient;
import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.entity.DocumentObj;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.service.ISearchService;
import com.wzr.rendisk.utils.DocumentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzr
 * @date 2023-06-10 0:15
 */
@Transactional(rollbackFor=Exception.class)
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    /**
     * 允许插入的文件类型
     */
    private static final String[] ALLOW_EXTENSION = {"txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", 
            "rtf", "csv", "html", "htm", "xml", "md", "odt", "ott", "ods", "ots", "odp", "otp", "odg", "otg", 
            "odm", "ott", "odc", "otc", "odw", "otw", "epub", "mobi", "azw", "azw3", "fb2", "djvu"};
    
    @Value("${my-config.elasticsearch.doc-index}")
    private String docIndex;
    
    @Autowired
    private IFileSystemService fileSystemService;
    @Autowired
    private MinioClientPlus minioClientPlus;
    @Autowired
    private ElasticSearchClient esClient;
    
    @Override
    public List<DocumentObj> loadAllFile(User user) {
        // 获取根目录下所有文件
        String root = "";
        List<FileInfo> fileInfos = fileSystemService.getFileListByPath(user.getId(), root);
        // 取出真实文件
        List<DocumentObj> realFiles = new ArrayList<>();
        for (FileInfo fileInfo: fileInfos) {
            boolean allow = FilenameUtils.isExtension(fileInfo.getFileName(), ALLOW_EXTENSION);
            if (! allow) {
                continue;
            }
            try {
                String fileName = fileInfo.getFileName();
                DocumentObj doc = new DocumentObj(user.getId(), fileInfo.getId(),
                        fileName, DocumentUtils.getFileExtension(fileName));
                InputStream inputStream = minioClientPlus.getFileStream(
                        minioClientPlus.getBucketByUsername(user.getUsername()),
                        fileInfo.getRealPath());
                // content字段一定要是base64
                byte[] docByte = IOUtils.toByteArray(inputStream);
                doc.setContent( DocumentUtils.getBase64(docByte) );
                realFiles.add(doc);
            } catch (Exception e) {
                e.printStackTrace();
                throw new GlobalException();
            }
        }
        // 写入es
        esClient.bulkInsertDocument(docIndex, realFiles);
        return realFiles;
    }

    @Override
    public void removeAllFile(User user) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf( user.getId() ));
        esClient.bulkDeleteDocument(docIndex, map);
    }


}
