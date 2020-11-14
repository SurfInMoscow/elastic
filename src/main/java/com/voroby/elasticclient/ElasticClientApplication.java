package com.voroby.elasticclient;

import com.voroby.elasticclient.domain.eom.ItemEom;
import com.voroby.elasticclient.domain.eom.UserEom;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableElasticsearchRepositories("com.voroby.elasticclient.repository")
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

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }

    @Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
                Arrays.asList(new ItemEomToMap(), new MapToItemEom()));
    }

    @WritingConverter
    static class ItemEomToMap implements Converter<ItemEom, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(ItemEom itemEom) {
            Map<String, Object> target = new LinkedHashMap<>();
            target.put("id", itemEom.getId());
            target.put("name", itemEom.getName());
            target.put("description", itemEom.getDescription());
            UserEom owner = itemEom.getOwner();

            if (owner != null) {
                target.put("ownerId", owner.getId());
            } else {
                target.put("ownerId", "");
            }

            return target;
        }
    }

    @ReadingConverter
    static class MapToItemEom implements Converter<Map<String, Object>, ItemEom> {

        @Override
        public ItemEom convert(Map<String, Object> stringObjectMap) {
            ItemEom itemEom = new ItemEom();
            itemEom.setId((String) stringObjectMap.get("id"));
            itemEom.setName((String) stringObjectMap.get("name"));
            itemEom.setDescription((String) stringObjectMap.get("description"));

            return itemEom;
        }
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