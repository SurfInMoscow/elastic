package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ElasticClientApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Qualifier("elasticResponseListener")
    @Autowired
    private ActionListener<IndexResponse> elasticResponseListener;

    private static Gson userGson;
    private static Gson itemGson;

    private static List<User> users = new ArrayList<>();
    private static List<Item> items = new ArrayList<>();

    @BeforeAll
    public static void populate() {
        User user = new User("user@ya.ru", "password");
        Item item = new Item("item1", "test item", user);
        Item item1 = new Item("item2", "test item", user);
        user.getItems().add(item);
        user.getItems().add(item1);
        User user1 = new User("super@ya.ru", "superpass");
        users.add(user);
        users.add(user1);
        items.add(item);
        items.add(item1);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Item.class, new ItemJsonAdapter());
        userGson = builder.create();
        builder.registerTypeAdapter(User.class, new UserJsonAdapter());
        itemGson = builder.create();
    }

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
