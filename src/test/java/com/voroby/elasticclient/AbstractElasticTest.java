package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AbstractElasticTest {
    @Autowired
    protected RestHighLevelClient restHighLevelClient;

    protected static Gson userGson;
    protected static Gson itemGson;

    protected static List<User> users = new ArrayList<>(100);
    protected static List<Item> items = new ArrayList<>(100);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @BeforeAll
    public static void populate() throws IOException, ExecutionException, InterruptedException {
        IntStream.range(0, 100).forEach(i -> {
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
        insert();
    }

    @AfterAll
    public static void deleteIndices() throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest();
        request.indices("users", "items");
        request.setQuery(QueryBuilders.matchAllQuery());
        RestHighLevelClient restHighLevelClient1 = getRestHighLevelClient();
        restHighLevelClient1.deleteByQuery(request, RequestOptions.DEFAULT);
         /*DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest();
        deleteIndexRequest.indices("users", "items");
        AcknowledgedResponse delete = restHighLevelClient1.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        assertTrue(delete.isAcknowledged());*/
    }


    private static void createIndices() {
        Future<?> usr = executor.submit(() -> index("users"));
        Future<?> itm = executor.submit(() -> index("items"));
        try {
            usr.get();
            itm.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void index(String index) {
        RestHighLevelClient restHighLevelClient1 = getRestHighLevelClient();
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);

        try {
            if (!restHighLevelClient1.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                CreateIndexRequest indexRequest = new CreateIndexRequest(index);
                indexRequest.settings(Settings.builder()
                        .put("index.number_of_shards", 5)
                        .put("index.number_of_replicas", 2));
                CreateIndexResponse createIndexResponse = restHighLevelClient1.indices().create(indexRequest, RequestOptions.DEFAULT);
                assertTrue(createIndexResponse.isAcknowledged());
                assertTrue(createIndexResponse.isShardsAcknowledged());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static RestHighLevelClient getRestHighLevelClient() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(ElasticClientApplication.class);
        applicationContext.refresh();
        return applicationContext.getBean(RestHighLevelClient.class);
    }

    private static void insert() {
        Future<?> usr = executor.submit(AbstractElasticTest::indexUsers);
        Future<?> itm = executor.submit(AbstractElasticTest::indexItems);
        try {
            usr.get();
            itm.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void indexUsers() {
        BulkRequest bulkRequest = new BulkRequest();
        users.forEach(user -> {
            IndexRequest request = new IndexRequest("users");
            request.id(user.getId());
            request.source(userGson.toJson(user), XContentType.JSON);
            bulkRequest.add(request);
        });
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            getRestHighLevelClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void indexItems() {
        BulkRequest bulkRequest = new BulkRequest();
        items.forEach(item -> {
            IndexRequest request = new IndexRequest("items");
            request.id(item.getId());
            request.source(itemGson.toJson(item), XContentType.JSON);
            bulkRequest.add(request);
        });
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            getRestHighLevelClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
