package com.wzr.rendisk.config.es;

import com.wzr.rendisk.core.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.function.BiConsumer;

/**
 * @author wzr
 * @date 2023-06-09 16:02
 */
@Configuration
@Slf4j
public class ElasticSearchConfig {

    @Value("${my-config.elasticsearch.url}")
    private String esHost;

    @Value("${my-config.elasticsearch.port}")
    private int esPort;
    
    /**
     * 获取ES操作对象，注入bean中
     * @return ES client对象
     */
    @Bean("myESClient")
    public RestHighLevelClient myElasticsearchClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(esHost, esPort, "http")
        ));
    }

    /**
     * 获取批量操作对象
     * 使用操作：bulkProcessor.add(new IndexRequest...)
     * 官方文档：https://www.elastic.co/guide/en/elasticsearch/client/java-api/2.3/java-docs-bulk-processor.html
     * @return 批量操作对象
     */
    @Bean("bulkProcessor")
    public BulkProcessor getBulkProcessor(RestHighLevelClient myESClient) {
        try {
            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                    log.info("[es批量操作] 尝试插入 {} 条数据...", request.numberOfActions());
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    log.info("[es批量操作] 数据批量插入成功");
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    log.error("[es批量操作] 数据批量插入失败!");
                }
            };
            return BulkProcessor.builder((request, bulkListener) ->
                    myESClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
                    // 执行了指定个数的request时，进行刷新
                    .setBulkActions(100)
                    // 内存到达8M时，进行刷新
                    .setBulkSize(new ByteSizeValue(8, ByteSizeUnit.MB))
                    // 每2秒进行刷新，不管有多少个request
                    .setFlushInterval(TimeValue.timeValueSeconds(2))
                    // 设置允许执行的并发请求数。
                    .setConcurrentRequests(2)
                    // 设置重试策略
                    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(1000), 3))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException();
        }
    }
}
