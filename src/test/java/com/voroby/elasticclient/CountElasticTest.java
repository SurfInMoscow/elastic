package com.voroby.elasticclient;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CountElasticTest extends AbstractElasticTest {
    @Test
    public void countTest() throws IOException {
        CountRequest countRequest = new CountRequest("items");
        countRequest.query(QueryBuilders.matchAllQuery());
        CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        System.out.printf("{items} Count: %d, Shards: %d, Successful Shards: %d%n", count.getCount(), count.getTotalShards(), count.getSuccessfulShards());

        countRequest = new CountRequest("users");
        countRequest.query(QueryBuilders.matchAllQuery());
        count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        System.out.printf("{users} Count: %d, Shards: %d, Successful Shards: %d", count.getCount(), count.getTotalShards(), count.getSuccessfulShards());
    }
}
