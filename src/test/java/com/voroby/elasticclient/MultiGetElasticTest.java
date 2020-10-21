package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiGetElasticTest extends AbstractElasticTest {
    @Test
    public void multiGetRequest() throws IOException {
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

    private Gson getGsonWithTypeAdapter(Type type, Object typeAdapter) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(type, typeAdapter);

        return  builder.create();
    }
}
