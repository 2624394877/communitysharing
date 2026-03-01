package com.taoxin.communitysharing.search.business.config;

import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchRestHighLevelClient {
    @Resource
    private ElasticsearchProperties elasticsearchProperties;

    private static final String COLON = ":";
    private static final String SCHEMA = "http";

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String address = elasticsearchProperties.getAddress();
        String[] split = address.split(COLON); // 192.168.1.1:9200
        String host = split[0]; // ip
        int port = Integer.parseInt(split[1]); // 端口
        HttpHost httpHost = new HttpHost(host, port, SCHEMA);

        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
