package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchElasticTest extends AbstractElasticTest {
    @Test
    public void search() throws IOException, InterruptedException {
        index();
        /*don't use this delay! I did it because my test computer is slow and
        local elastic didn't finished indexing before search query starts.*/
        Thread.sleep(5000);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("users", "items");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /*
        To source simple match query by single value for filed. It's also possible to add several values check.
        sourceBuilder.query(QueryBuilders.matchQuery("name", "GetItem2"));

        Alternative variant.
        MatchQueryBuilder match = new MatchQueryBuilder("name", "GetItem2");

        In this case we refer to field in nested object in our document.
        MatchQueryBuilder match1 = new MatchQueryBuilder("items.name", "GetItem2");
        */

        //add match query for value in several fields in our indices.
        MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("Item2", "name", "items.name");
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(1000);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(10));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        List<SearchHit> list = Arrays.asList(hits.getHits());
        List<User> foundUsers = new ArrayList<>();
        List<Item> foundItems = new ArrayList<>();
        list.forEach(hit -> {
            String hitAsString = hit.getSourceAsString();
            Gson gson;
            switch (hit.getIndex()) {
                case "users":
                    gson = getGsonWithTypeAdapter(User.class, new UserJsonAdapter());
                    foundUsers.add(gson.fromJson(hitAsString, User.class));
                    break;
                case "items":
                    gson = getGsonWithTypeAdapter(Item.class, new ItemJsonAdapter());
                    foundItems.add(gson.fromJson(hitAsString, Item.class));
                    break;
            }
        });

        assertTrue(foundUsers.stream().anyMatch(found -> found.equals(users.get(0))));
        assertTrue(foundItems.stream().anyMatch(found -> found.equals(items.get(1))));
    }

    private void index() throws IOException {
        indexUsers();
        indexItems();
    }

    private void indexUsers() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        users.forEach(user -> {
            IndexRequest request = new IndexRequest("users");
            request.id(user.getId());
            request.source(userGson.toJson(user), XContentType.JSON);
            bulkRequest.add(request);
        });
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    private void indexItems() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        items.forEach(item -> {
            IndexRequest request = new IndexRequest("items");
            request.id(item.getId());
            request.source(itemGson.toJson(item), XContentType.JSON);
            bulkRequest.add(request);
        });
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    private Gson getGsonWithTypeAdapter(Type type, Object typeAdapter) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(type, typeAdapter);

        return  builder.create();
    }
}