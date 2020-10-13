package com.voroby.elasticclient;

import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteElasticTest extends AbstractElasticTest {
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
    public void deleteRequest() throws IOException {
        index();
        Item item = items.iterator().next();
        DeleteRequest delete = new DeleteRequest("items", item.getId());
        DeleteResponse deleteResponse = restHighLevelClient.delete(delete, RequestOptions.DEFAULT);
        assertEquals(200, deleteResponse.status().getStatus());
    }

    @Test
    public void deleteNotExist() throws IOException {
        Item mockItem = mock(Item.class);
        when(mockItem.getId()).thenReturn(UUID.randomUUID().toString());
        DeleteRequest delete = new DeleteRequest("items", mockItem.getId());
        DeleteResponse deleteResponse = restHighLevelClient.delete(delete, RequestOptions.DEFAULT);
        assertEquals(404, deleteResponse.status().getStatus());
        assertEquals("not_found", deleteResponse.getResult().getLowercase());
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