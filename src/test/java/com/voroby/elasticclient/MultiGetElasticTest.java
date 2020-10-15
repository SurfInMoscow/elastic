package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MultiGetElasticTest extends AbstractElasticTest {
    private static List<User> users = new ArrayList<>();
    private static List<Item> items = new ArrayList<>();

    @BeforeAll
    public static void populate() throws IOException {
        User user = new User("user@ya.ru", "password");
        Item item = new Item("GetItem1", "test item", user);
        Item item1 = new Item("GetItem2", "test item", user);
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
    public void multiGetRequest() throws IOException {
        index();
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        users.forEach(user -> multiGetRequest.add(new MultiGetRequest.Item("users", user.getId())));
        items.forEach(item -> multiGetRequest.add(new MultiGetRequest.Item("items", item.getId())));
        MultiGetResponse mget = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);

        List<Item> getItems = new ArrayList<>();
        List<User> getUsers = new ArrayList<>();
        Iterator<MultiGetItemResponse> iterator = mget.iterator();

        while (iterator.hasNext()) {
            MultiGetItemResponse response = iterator.next();
            Gson gson;
            switch (response.getIndex()) {
                case "users":
                    gson = getGsonWithTypeAdapter(User.class, new UserJsonAdapter());
                    getUsers.add(gson.fromJson(gsonString(response.getResponse()), User.class));
                    break;
                case "items":
                    gson = getGsonWithTypeAdapter(Item.class, new ItemJsonAdapter());
                    getItems.add(gson.fromJson(gsonString(response.getResponse()), Item.class));
                    break;
            }
        }

        assertTrue(getItems.containsAll(items));
        assertTrue(getUsers.containsAll(users));
        assertEquals(items, getItems);
        assertEquals(users, getUsers);
    }

    private String gsonString(GetResponse getResponse) {
        return getResponse.getSourceAsString();
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
