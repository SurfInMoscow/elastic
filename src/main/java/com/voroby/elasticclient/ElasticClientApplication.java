package com.voroby.elasticclient;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.stereotype.Component;

import java.time.Duration;

@SpringBootApplication
public class ElasticClientApplication extends AbstractElasticsearchConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(ElasticClientApplication.class, args);
    }

    /*@Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient
                .builder(new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"),
                        new HttpHost("localhost", 9202, "http")));
    }*/

    @Bean
    public ActionListener<IndexResponse> elasticResponseListener() {
        return new ElasticResponseListener();
    }

    // https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.clients
    @Bean(name = "restHighLevelClient")
    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200", "localhost:9201", "localhost:9202")
                .withConnectTimeout(Duration.ofSeconds(10))
                .withSocketTimeout(Duration.ofSeconds(7))
                .build();

        return RestClients.create(clientConfiguration).rest();
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