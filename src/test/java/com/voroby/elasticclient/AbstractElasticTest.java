package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AbstractElasticTest {
    @Autowired
    protected RestHighLevelClient restHighLevelClient;

    protected static Gson userGson;
    protected static Gson itemGson;

    protected static List<User> users = new ArrayList<>(200);
    protected static List<Item> items = new ArrayList<>(200);

    @BeforeAll
    public static void populate() throws IOException {
        IntStream.range(0,200).forEach(i -> {
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
        });

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Item.class, new ItemJsonAdapter());
        userGson = builder.create();
        builder.registerTypeAdapter(User.class, new UserJsonAdapter());
        itemGson = builder.create();
        createIndices();
    }


    private static void createIndices() throws IOException {
        index("users");
        index("items");
    }

    private static void index(String index) throws IOException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(ElasticClientApplication.class);
        applicationContext.refresh();
        RestHighLevelClient restHighLevelClient1 = applicationContext.getBean(RestHighLevelClient.class);
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);

        if (!restHighLevelClient1.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            CreateIndexRequest indexRequest = new CreateIndexRequest(index);
            indexRequest.settings(Settings.builder()
                    .put("index.number_of_shards", 6)
                    .put("index.number_of_replicas", 2));
            CreateIndexResponse createIndexResponse = restHighLevelClient1.indices().create(indexRequest, RequestOptions.DEFAULT);
            assertTrue(createIndexResponse.isAcknowledged());
            assertTrue(createIndexResponse.isShardsAcknowledged());
        }
    }
}
