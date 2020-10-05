package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import org.elasticsearch.action.ActionListener;
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

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ElasticClientApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Qualifier("elasticResponseListener")
    @Autowired
    private ActionListener<IndexResponse> elasticResponseListener;

    private static List<User> users = new ArrayList<>();

    @BeforeAll
    public static void populate() {
        User user = new User("user@ya.ru", "password");
        user.getItems().add(new Item("item1", "test item", user));
        user.getItems().add(new Item("item2", "test item", user));
        User user1 = new User("super@ya.ru", "superpass");
        users.add(user);
        users.add(user1);
    }

    @Test
    public void indexUsers() {
        Gson gson = new Gson();
        users.forEach(user -> {
            IndexRequest request = new IndexRequest("users");
            request.id(user.getId());
            request.source("", XContentType.JSON);
            restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, elasticResponseListener);
        });
    }
}
