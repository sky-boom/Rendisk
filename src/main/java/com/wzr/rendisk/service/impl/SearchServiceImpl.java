package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.config.es.ElasticSearchClient;
import com.wzr.rendisk.config.minio.MinioClientPlus;
import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.dto.SearchDto;
import com.wzr.rendisk.entity.DocumentObj;
import com.wzr.rendisk.entity.FileInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.IFileSystemService;
import com.wzr.rendisk.service.ISearchService;
import com.wzr.rendisk.utils.DocumentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

/**
 * @author wzr
 * @date 2023-06-10 0:15
 */
@Transactional(rollbackFor=Exception.class)
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    /**
     * 规定单个存入elasticsearch的文档大小为 20MB
     * 超过20MB咋办? 分割pdf.
     */
    public static final int LIMITED_BYTE = 20 * 1024 * 1024;
    
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
        List<DocumentObj> realFiles = new ArrayList<>();
        for (FileInfo fileInfo: fileInfos) {
            boolean allow = FilenameUtils.isExtension(fileInfo.getFileName(), ALLOW_EXTENSION);
            if (! allow) {
                continue;
            }
            try (InputStream inputStream = minioClientPlus.getFileStream(
                    minioClientPlus.getBucketByUsername(user.getUsername()),
                    fileInfo.getRealPath())) {
                String fileName = fileInfo.getFileName();
                DocumentObj doc = new DocumentObj(user.getId(), fileInfo.getId(),
                        fileName, DocumentUtils.getFileExtension(fileName));
                // 如果字节数 > 规定大小, 应分割pdf
                byte[] docByte = IOUtils.toByteArray(inputStream);
                if (docByte.length > LIMITED_BYTE) {
                    realFiles.addAll( splitPdf(inputStream, docByte.length / LIMITED_BYTE, doc) );
                } else {
                    // content字段一定要是base64
                    doc.setContent(DocumentUtils.getBase64(docByte));
                    realFiles.add(doc);
                }
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

    @Override
    public List<DocumentObj> search(User user, SearchDto searchDto) {
        // 高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("attachment.content")
                .preTags("<font color='red' font-weight='bold'>")
                .postTags("</font>");
        // 普通全索引查询
        SearchSourceBuilder searchSourceBuilder = 
                new SearchSourceBuilder()
                        .query(QueryBuilders.matchQuery("attachment.content", searchDto.getKeyword()).analyzer("ik_smart"))
                        .highlighter(highlightBuilder);
        return buildResult(esClient.selectDocumentList(docIndex, searchSourceBuilder));
    }

    @Override
    public List<DocumentObj> testSearch(String keyword) {
        // 高亮查询，关键词添加红色样式
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("attachment.content")
                .preTags("<font color='red' font-weight='bold'>")
                .postTags("</font>");
        // 普通全索引查询
        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(QueryBuilders.matchQuery("attachment.content", keyword).analyzer("ik_smart"))
                        .highlighter(highlightBuilder);
        SearchHit[] searchHits = esClient.selectDocumentList("docwrite", searchSourceBuilder);
        // 处理每一条记录(每一个文档)，获得高亮文本。
        List<DocumentObj> results = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            DocumentObj obj = new DocumentObj();
            obj.setDocId( ((Integer) sourceAsMap.get("docId")).longValue() );
            obj.setDocName( (String) sourceAsMap.get("docName") );
            
            HighlightField contentHighlightField = hit.getHighlightFields().get("attachment.content");
            // 对于一个文档，它的高亮文本有多个结果，这里只拼接前2个结果。
            String highLightMessage = contentHighlightField.fragments()[0].toString()
                    + "  " + contentHighlightField.fragments()[1].toString();
            obj.setContent(highLightMessage);
            results.add(obj);
        }
        return results;
    }

    @Override
    public boolean testLoadDocument() {
        // 用本地文档进行测试
        try {
            // 加载文件，得到base64
            File file = new File("D:\\桌面文件\\es介绍.docx");
            InputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = IOUtils.toByteArray(fileInputStream);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            
            // 向es添加文档
            DocumentObj obj = new DocumentObj();
            obj.setUserId(1001L);
            obj.setDocId(666L);
            obj.setDocName("es介绍.docx");
            obj.setDocType("docx");
            obj.setContent(base64);
            return esClient.insertDocument("docwrite", obj);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 把elasticsearch查询结果,转化为对应的对象列表
     * @param hits elasticsearch查询结果
     * @return DocumentObj 对象
     */
    private List<DocumentObj> buildResult(SearchHit[] hits) {
        List<DocumentObj> documentObjs = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            // 获得查询到的ES文档字段
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            Long docId = ((Integer) sourceAsMap.get("docId")).longValue();
//            Long teamId = ((Integer) sourceAsMap.get("teamId")).longValue();
            String docName = (String) sourceAsMap.get("docName"); 
            String docType = (String) sourceAsMap.get("docType");
            String content = (String) ((Map<String, Object>) (sourceAsMap.get("attachment"))).get("content");
            
            // 映射到java对象
            DocumentObj obj = new DocumentObj();
            obj.setDocId(docId);
//            obj.setTeamId(teamId);
            obj.setDocName(docName);
            obj.setDocType(docType);
            
            // 获得高亮片段的前2段
            HighlightField contentHighlightField = searchHit.getHighlightFields().get("attachment.content");
            if( contentHighlightField == null ){
                obj.setContent(content);
            } else {
                String highLightMessage = contentHighlightField.fragments()[0].toString()
                        + "  " + contentHighlightField.fragments()[1].toString();
                obj.setContent(highLightMessage);
            }
            documentObjs.add(obj);
        }
        return documentObjs;
    }

    /**
     * 把 pdf 文件进行拆分
     * @param inputStream pdf文件流
     * @param sliceCount 切片个数
     * @param obj 用于
     * @return elasticsearch插入对象列表
     */
    private List<DocumentObj> splitPdf(InputStream inputStream, int sliceCount, DocumentObj obj) {
        
//        try (PDDocument originPdf = PDDocument.load( DocumentUtils.input2Output(inputStream)) )  {
        try (PDDocument originPdf = new PDDocument()) {
            originPdf.save( DocumentUtils.input2Output(inputStream) );
            // 获得pdf页数
            int totalSize = originPdf.getNumberOfPages();
            // 获得每个分片的pdf页数
            int pageLength = (int) Math.ceil( (double) totalSize / sliceCount );
            List<DocumentObj> objList = new ArrayList<>();
            // 原来pdf的页: 1,2,3,4,5,6,7,8  拆分pdf1: 1,2,3,4  拆分pdf2: 5,6,7,8
            for (int i = 0; i < sliceCount; i++) {
                PDDocument pdf = new PDDocument();
                int endPage = Math.min((i + 1) * pageLength, totalSize);
                // 原pdf的指定页数添加到一个pdf文件内
                for (int j = i * pageLength; j < endPage; j ++) {
                    pdf.addPage(originPdf.getPage(j));
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                pdf.save(outputStream);
                // 分段的pdf拥有相同的属性
                DocumentObj currObj = obj.clone();
                currObj.setContent( DocumentUtils.getBase64(outputStream.toByteArray()) );
                objList.add( currObj );
                pdf.close();
            }
            return objList;
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new GlobalException();
        }
    }
}
