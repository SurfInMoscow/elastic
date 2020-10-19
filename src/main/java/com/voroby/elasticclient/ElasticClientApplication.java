package com.voroby.elasticclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ElasticClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticClientApplication.class, args);
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient
                .builder(new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"),
                        new HttpHost("localhost", 9202, "http")));
    }

    @Bean
    public ActionListener<IndexResponse> elasticResponseListener() {
        return new ElasticResponseListener();
    }

    @Component
    @Slf4j
    public static class ElasticResponseListener implements ActionListener<IndexResponse> {

        @Override
        public void onResponse(IndexResponse indexResponse) {
            log.info("Message with ID:{} {}, index:{}",
                    indexResponse.getId(), indexResponse.getResult().name(), indexResponse.getIndex());
        }

        @Override
        public void onFailure(Exception e) {
            log.error("Exception message: {}", e.getMessage());
        }
    }
}