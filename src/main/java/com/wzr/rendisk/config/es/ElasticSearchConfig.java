package com.wzr.rendisk.config.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wzr
 * @date 2023-06-09 16:02
 */
@Configuration
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
}
