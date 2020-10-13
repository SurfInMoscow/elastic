package com.voroby.elasticclient;

import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateElasticTest extends AbstractElasticTest {
    private static List<User> users = new ArrayList<>();
    private static List<Item> items = new ArrayList<>();

    @BeforeAll
    public static void populate() throws IOException {
        User user = new User("user@ya.ru", "password");
        Item item = new Item("Item1", "test item", user);
        Item item1 = new Item("Item2", "test item", user);
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
    public void updateRequest() throws IOException {
        index();
        User user = users.iterator().next();
        GetRequest userReq = new GetRequest("users", user.getId());

        GetResponse userResponse = restHighLevelClient.get(userReq, RequestOptions.DEFAULT);
        long version = userResponse.getVersion();

        Map<String, Object> jsonMap = new HashMap<>();
        user.setEmail("newmail@gmail.com");
        jsonMap.put("email", user.getEmail());
        UpdateRequest updateRequest = new UpdateRequest("users", user.getId()).doc(jsonMap);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        assertEquals(200, updateResponse.status().getStatus());
        version++;
        assertEquals(version, updateResponse.getVersion());
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
}
