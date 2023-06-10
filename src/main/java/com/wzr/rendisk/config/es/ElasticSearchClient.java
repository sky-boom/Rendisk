package com.wzr.rendisk.config.es;

import com.alibaba.fastjson.JSON;
import com.wzr.rendisk.core.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.lang.reflect.Method;

import java.io.IOException;
import java.util.*;

/**
 * es工具类
 * 参考：https://blog.csdn.net/qq_38374397/article/details/123814908
 * 
 * @author wzr
 * @date 2023-06-09 16:13
 */
@Component
@Slf4j
public class ElasticSearchClient {
    
    @Autowired
    @Qualifier("myESClient")
    private RestHighLevelClient restHighLevelClient;

    /**
     * 默认类型
     */
    public static final String DEFAULT_TYPE = "_doc";

    /**
     * set方法前缀
     */
    public static final String SET_METHOD_PREFIX = "set";

    /**
     * 返回状态-CREATED
     */
    public static final String RESPONSE_STATUS_CREATED = "CREATED";

    /**
     * 返回状态-OK
     */
    public static final String RESPONSE_STATUS_OK = "OK";

    /**
     * 需要过滤的文档数据
     */
    public static final String[] IGNORE_KEY = {"@version","type"};

    /**
     * 超时时间 5s
     */
    public static final TimeValue TIME_VALUE_SECONDS = TimeValue.timeValueSeconds(3);

    /**
     * (批量)超时时间 5s
     */
    public static final TimeValue BULK_TIME_VALUE_SECONDS = TimeValue.timeValueSeconds(6);

    /**
     * 文档批量操作 之 批量插入
     */
    public static final Integer BULK_INSERT_TYPE = 1;

    /**
     * 文档批量操作 之 批量更新
     */
    public static final Integer BULK_UPDATE_TYPE = 2;

    /**
     * 文档批量操作 之 批量删除
     */
    public static final Integer BULK_DELETE_TYPE = 3;

    /**
     * 创建索引
     * @param index
     * @return
     */
    public  boolean createIndex(String index) throws IOException {
        if (!isIndexExist(index)) {
            log.debug("[es] Index is not exits!");
        }
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        CreateIndexResponse response = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        log.debug("[es] 执行建立成功？" + response.isAcknowledged());
        return response.isAcknowledged();
    }

    /**
     * 删除索引
     *
     * @param index
     * @return
     */
    public boolean deleteIndex(String index) throws IOException {
        if (!isIndexExist(index)) {
            log.debug("[es] Index is not exits!");
        }
        DeleteIndexRequest request = new DeleteIndexRequest("mdx_index");
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            log.debug("[es] delete index " + index + "  successfully!");
        } else {
            log.debug("[es] Fail to delete index " + index);
        }
        return response.isAcknowledged();
    }

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     */
    public  boolean isIndexExist(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        if (exists) {
            log.debug("[es] Index [" + index + "] is exist!");
        } else {
            log.debug("[es] Index [" + index + "] is not exist!");
        }
        return exists;
    }

    /**
     * 指定文档是否存在
     *
     * @param index 索引
     * @param id    文档id
     */
    public boolean isExists(String index, String id) {
        return isExists(index, DEFAULT_TYPE, id);
    }

    /**
     * 指定文档是否存在
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档id
     */
    public boolean isExists(String index, String type, String id) {
        GetRequest request = new GetRequest(index, type, id);
        try {
            GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据id查询文档
     * @param index 索引
     * @param id    文档id
     * @param clazz 转换目标Class对象
     * @return 对象
     */
    public <T> T selectDocumentById(String index, String id, Class<T> clazz) {
        return selectDocumentById(index, DEFAULT_TYPE, id, clazz);
    }

    /**
     * 根据id查询文档
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档id
     * @param clazz 转换目标Class对象
     * @return 对象
     */
    public <T> T selectDocumentById(String index, String type, String id, Class<T> clazz) {
        try {
            type = type == null || type.equals("") ? DEFAULT_TYPE : type;
            GetRequest request = new GetRequest(index, type, id);
            GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            if (response.isExists()) {
                Map<String, Object> sourceAsMap = response.getSourceAsMap();
                return dealObject(sourceAsMap, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *（筛选条件）获取数据集合
     * 如果使用排序则 sourceBuilder.sort("name",SortOrder.DESC)
     * 如果使用高亮则 :
     * HighlightBuilder highlightBuilder = new HighlightBuilder();
     * highlightBuilder.field("");
     * sourceBuilder.highlighter(highlightBuilder);
     * @param index         索引
     * @param sourceBuilder 请求条件
     * @param clazz         转换目标Class对象
     */
    public <T> List<T> selectDocumentList(String index, SearchSourceBuilder sourceBuilder, Class<T> clazz) {
        try {
            SearchRequest request = new SearchRequest(index);
            if (sourceBuilder != null) {
                // 返回实际命中数
                sourceBuilder.trackTotalHits(true);
                request.source(sourceBuilder);
            }
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            if (response.getHits() != null) {
                List<T> list = new ArrayList<>();
                SearchHit[] hits = response.getHits().getHits();
                for (SearchHit documentFields : hits) {
                    Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
                    // 高亮结果集特殊处理 -- 高亮信息会显示在highlight标签下  需要将实体类中的字段进行替换
                    Map<String, Object> map = this.highlightBuilderHandle(sourceAsMap, documentFields);
                    list.add(dealObject(map, clazz));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 插入/修改文档信息
     * @param index 索引
     * @param data  数据
     */
    public boolean insertDocument(String index, Object data) {
        try {
            String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            IndexRequest request = new IndexRequest(index);
            request.timeout(TIME_VALUE_SECONDS);
            request.id(id);
            request.source(JSON.toJSONString(data), XContentType.JSON);
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            log.debug("[es] 插入文档的响应状态: status:{},id:{}", response.status().getStatus(), response.getId());
            String status = response.status().toString();
            if (RESPONSE_STATUS_CREATED.equals(status) || RESPONSE_STATUS_OK.equals(status)) {
                log.debug("[es] 插入文档成功! ");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[es] 插入文档失败");
        }
        return false;
    }

    /**
     * 批量插入文档
     * @param index 索引
     * @param dataList  数据
     */
    public void bulkInsertDocument(String index, List<?> dataList) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (Object obj : dataList) {
                bulkRequest.timeout(BULK_TIME_VALUE_SECONDS);
                String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                // 注意，必须设置文本提取的管道！
                bulkRequest.add(new IndexRequest(index).setPipeline("attachment")
                        .id(uuid).source(JSON.toJSONString(obj), XContentType.JSON));
            }
            // 执行批量操作
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            boolean isSuccess = !bulkResponse.hasFailures();
            String status = bulkResponse.status().toString();
            log.debug("[es] 批量插入的响应状态: status:{}, success:{}", status, isSuccess);
            if (isSuccess) {
                log.debug("[es] 批量插入操作成功! ");
            }
        } catch (Exception e) {
            log.error("[es] 批量插入文档失败");
            e.printStackTrace();
            throw new GlobalException();
        }
    }
    
    /**
     * 删除文档信息
     *
     * @param index 索引
     * @param id    文档id
     */
    public boolean deleteDocument(String index, String id) {
        try {
            DeleteRequest request = new DeleteRequest(index, id);
            DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            log.debug("[es] 删除文档的响应状态: status:{},id:{}", response.status().getStatus(), response.getId());
            String status = response.status().toString();
            if (RESPONSE_STATUS_OK.equals(status)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量删除文档
     * @param index 索引
     * @param query 查询条件（键值对）
     */
    public void bulkDeleteDocument(String index, Map<String, String> query) {
        try {
            DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(index);
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (Map.Entry<String, String> entry : query.entrySet()) {
                // 这里假设.must的内容已经作用在原对象上了。（未验证）
                boolQueryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
            }
            deleteRequest.setQuery(boolQueryBuilder);
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
            log.debug("[es] 批量删除响应: {}", response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[es] 批量插入文档失败");
        }
    }


    /**
     * 更新文档信息
     *
     * @param index 索引
     * @param id    文档id
     * @param data  数据
     */
    public boolean updateDocument(String index, String id, Object data) {
        try {
            UpdateRequest request = new UpdateRequest(index, id);
            request.doc(data, XContentType.JSON);
            UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            log.debug("[es] updateDocument response status:{},id:{}", response.status().getStatus(), response.getId());
            String status = response.status().toString();
            if (RESPONSE_STATUS_OK.equals(status)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 批量操作(新增)
     * @param index    索引
     * @param opType   操作类型 PATCH_OP_TYPE_*
     * @param dataList 数据集 新增修改需要传递
     * @param timeout  超时时间 单位为秒
     */
    public boolean patch(String index, String opType, List<Object> dataList, long timeout) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.timeout(TimeValue.timeValueSeconds(timeout));
            if (dataList != null && dataList.size() > 0) {
                if ("insert".equals(opType)) {
                    for (Object obj : dataList) {
                        bulkRequest.add(
                                new IndexRequest(index)
                                        .source(JSON.toJSONString(obj), XContentType.JSON)
                        );
                    }
                }
                BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                if (!response.hasFailures()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *（筛选条件）获取数据集合分页
     * 如果使用排序则 sourceBuilder.sort("name",SortOrder.DESC)
     * 如果使用高亮则 :
     * HighlightBuilder highlightBuilder = new HighlightBuilder();
     * highlightBuilder.field("");
     * sourceBuilder.highlighter(highlightBuilder);
     * @param index         索引
     * @param sourceBuilder 请求条件
     * @param clazz         转换目标Class对象
     */
    /*
    public <T> EsPage selectDocumentPage(String index,SearchSourceBuilder sourceBuilder, int startPage, int pageSize , Class<T> clazz) {
        try {
            SearchRequest request = new SearchRequest(index);
            if (sourceBuilder != null) {
                // 返回实际命中数
                sourceBuilder.from(startPage);
                sourceBuilder.size(pageSize);
                sourceBuilder.explain(true);
                sourceBuilder.trackTotalHits(true);
                request.source(sourceBuilder);
            }
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits() != null) {
                long totalHits = Arrays.stream(response.getHits().getHits()).count();
                long length = response.getHits().getHits().length;

                List<T> list = new ArrayList<>();
                SearchHit[] hits = response.getHits().getHits();
                for (SearchHit documentFields : hits) {
                    Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
                    // 高亮结果集特殊处理 -- 高亮信息会显示在highlight标签下  需要将实体类中的字段进行替换
                    Map<String, Object> map = this.highlightBuilderHandle(sourceAsMap, documentFields);
                    list.add(dealObject(map, clazz));
                }
                return new EsPage(startPage, pageSize, (int) totalHits, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     * 高亮结果集 特殊处理
     * @param sourceAsMap
     * @param documentFields
     */
    private Map<String, Object> highlightBuilderHandle(Map<String, Object> sourceAsMap,SearchHit documentFields){
        // 将高亮的字段替换到sourceAsMap中
        Map<String, HighlightField> fieldMap = documentFields.getHighlightFields();
        Set<Map.Entry<String, Object>> entries = sourceAsMap.entrySet();
        entries.forEach(source -> {
            if (fieldMap.containsKey(source.getKey())){
                Text[] fragments = fieldMap.get(source.getKey()).getFragments();
                if (fragments != null){
                    for (Text str : fragments) {
                        source.setValue(str.string());
                    }
                }
            }
        });
        return sourceAsMap;
    }

//    /**
//     * 高亮结果集 特殊处理
//     *
//     * @param searchResponse
//     * @param highlightField
//     */
//    private List<Map<String, Object>> setSearchResponse(SearchResponse searchResponse, String highlightField) {
//        List<Map<String, Object>> sourceList = new ArrayList<>();
//        StringBuffer stringBuffer = new StringBuffer();
//
//        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
//            searchHit.getSourceAsMap().put("id", searchHit.getId());
//
//            if (StringUtils.isNotEmpty(highlightField)) {
//
//                System.out.println("遍历 高亮结果集，覆盖 正常结果集" + searchHit.getSourceAsMap());
//                Text[] text = searchHit.getHighlightFields().get(highlightField).getFragments();
//
//                if (text != null) {
//                    for (Text str : text) {
//                        stringBuffer.append(str.string());
//                    }
//                    //遍历 高亮结果集，覆盖 正常结果集
//                    searchHit.getSourceAsMap().put(highlightField, stringBuffer.toString());
//                }
//            }
//            sourceList.add(searchHit.getSourceAsMap());
//        }
//        return sourceList;
//    }


    /**
     * 将文档数据转化为指定对象
     *
     * @param sourceAsMap 文档数据
     * @param clazz       转换目标Class对象
     * @return
     */
    private static <T> T dealObject(Map<String, Object> sourceAsMap, Class<T> clazz) {
        try {
            ignoreSource(sourceAsMap);
            Iterator<String> keyIterator = sourceAsMap.keySet().iterator();
            T t = clazz.newInstance();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                String replaceKey = key.replaceFirst(key.substring(0, 1), key.substring(0, 1).toUpperCase());
                Method method = null;
                try {
                    method = clazz.getMethod(SET_METHOD_PREFIX + replaceKey, sourceAsMap.get(key).getClass());
                }catch (NoSuchMethodException e) {
                    continue;
                }
                method.invoke(t, sourceAsMap.get(key));
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 剔除指定文档数据,减少不必要的循环
     *
     * @param map 文档数据
     */
    private static void ignoreSource(Map<String, Object> map) {
        for (String key : IGNORE_KEY) {
            map.remove(key);
        }
    }
}
