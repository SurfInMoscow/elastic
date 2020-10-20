package com.voroby.elasticclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;
import com.voroby.elasticclient.json.ItemJsonAdapter;
import com.voroby.elasticclient.json.UserJsonAdapter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetElasticTest extends AbstractElasticTest {
    @Test
    public void basicGetRequest() throws IOException {
        index();
        User user = users.get(0);
        Item item = user.getItems().iterator().next();
        GetRequest userReq = new GetRequest("users", user.getId());
        GetRequest itemReq = new GetRequest("items", item.getId());

        Gson gson;
        GetResponse userResponse = restHighLevelClient.get(userReq, RequestOptions.DEFAULT);
        String userJson = userResponse.getSourceAsString();
        gson = getGsonWithTypeAdapter(User.class, new UserJsonAdapter());
        User userFromJson = gson.fromJson(userJson, User.class);
        assertEquals(user, userFromJson);

        GetResponse itemResponse = restHighLevelClient.get(itemReq, RequestOptions.DEFAULT);
        String itemJson = itemResponse.getSourceAsString();
        gson = getGsonWithTypeAdapter(Item.class, new ItemJsonAdapter());
        Item itemFromJson = gson.fromJson(itemJson, Item.class);
        assertEquals(item, itemFromJson);
    }

    @Test
    public void getWithField() throws IOException {
        index();
        User user = users.get(0);
        GetRequest userReq = new GetRequest("users", user.getId());
        String[] includes = new String[]{"email", "id"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext context = new FetchSourceContext(true, includes, excludes);
        userReq.fetchSourceContext(context);

        GetResponse getResponse = restHighLevelClient.get(userReq, RequestOptions.DEFAULT);

        JsonElement jsonElement = JsonParser.parseString(getResponse.getSourceAsString());

        assertEquals(user.getId(), jsonElement.getAsJsonObject().get("id").getAsString());
        assertEquals(user.getEmail(), jsonElement.getAsJsonObject().get("email").getAsString());
    }

    @Test
    public void getWithException() throws IOException {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID().toString());

        GetRequest userReq = new GetRequest("not_exist", mockUser.getId());

        assertThrows(ElasticsearchException.class, () -> restHighLevelClient.get(userReq, RequestOptions.DEFAULT));

        GetRequest userReq1 = new GetRequest("users", mockUser.getId());
        GetResponse response = restHighLevelClient.get(userReq1, RequestOptions.DEFAULT);

        assertFalse(response.isExists());
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

    private void index() throws IOException {
        indexUsers();
        indexItems();
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
