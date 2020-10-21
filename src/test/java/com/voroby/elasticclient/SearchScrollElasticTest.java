package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchScrollElasticTest extends AbstractElasticTest {
    @Test
    public void searchScroll() throws IOException {
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("users", "items");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("Item1", "name", "items.name");
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(500); //for testing
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] hits = searchResponse.getHits().getHits();

        List<User> foundUsers = new ArrayList<>();
        List<Item> foundItems = new ArrayList<>();

        while (hits != null && hits.length > 0) {
            Arrays.stream(hits).forEach(hit -> {
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
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            hits = searchResponse.getHits().getHits();
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        assertTrue(succeeded);
        foundItems.forEach(item -> assertEquals("item1", item.getName().toLowerCase()));
        foundUsers.forEach(user -> assertEquals("item1", user.getItems().stream()
                .filter(item -> item.getName().toLowerCase().equals("item1")).findFirst().get().getName().toLowerCase()));
    }

    private Gson getGsonWithTypeAdapter(Type type, Object typeAdapter) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(type, typeAdapter);

        return builder.create();
    }
}
