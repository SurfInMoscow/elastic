package com.voroby.elasticclient;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class IndexElasticTest extends AbstractElasticTest {
    @Test
    public void indexUsers() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        users.forEach(user -> {
            IndexRequest request = new IndexRequest("users");
            request.id(user.getId());
            request.source(userGson.toJson(user), XContentType.JSON);
            bulkRequest.add(request);
            /*try {
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, elasticResponseListener);
        });
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void indexItems() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        items.forEach(item -> {
            IndexRequest request = new IndexRequest("items");
            request.id(item.getId());
            request.source(itemGson.toJson(item), XContentType.JSON);
            bulkRequest.add(request);
        });
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
