package com.voroby.elasticclient;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MultiSearchElasticTest extends AbstractElasticTest {
    @Test
    public void multiSearch() throws IOException {
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        SearchRequest searchRequest1 = new SearchRequest("users");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("items.name.keyword", "Item1"));
        sourceBuilder.size(1000);
        searchRequest1.source(sourceBuilder);
        multiSearchRequest.add(searchRequest1);
        SearchRequest searchRequest2 = new SearchRequest("items");
        sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("name.keyword", "Item1"));
        sourceBuilder.size(1000);
        searchRequest2.source(sourceBuilder);
        multiSearchRequest.add(searchRequest2);

        MultiSearchResponse msearch = restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
        List<MultiSearchResponse.Item> items = Arrays.asList(msearch.getResponses());
        items.forEach(item -> {
            List<SearchHit> hits = Arrays.asList(item.getResponse().getHits().getHits());
            hits.forEach(hit -> Assertions.assertTrue(hit.getSourceAsString().contains("Item1")));
        });
    }
}
